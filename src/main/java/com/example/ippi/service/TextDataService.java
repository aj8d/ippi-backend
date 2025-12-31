package com.example.ippi.service;

import com.example.ippi.entity.TextData;
import com.example.ippi.dto.DailyStats;
import com.example.ippi.repository.TextDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TextDataService - テキストデータと作業時間を管理するサービスクラス
 * 
 * 📚 このクラスの役割：
 * - データベースとのやり取り（CRUD操作）を行うビジネスロジック層
 * - コントローラーから呼び出され、リポジトリを使ってデータを操作
 * 
 * 💡 サービス層とは？
 * MVC（Model-View-Controller）アーキテクチャにおいて、
 * ビジネスロジック（業務処理）を担当する層。
 * コントローラー → サービス → リポジトリ → データベース
 * という流れでデータが処理される。
 */
@Service
public class TextDataService {

    /**
     * 📚 @Autowired とは？
     * Spring Frameworkの依存性注入（DI: Dependency Injection）機能。
     * Springが自動的にTextDataRepositoryのインスタンスを作成し、
     * このフィールドに「注入」してくれる。
     * 手動でnewする必要がなくなり、テストも容易になる。
     */
    @Autowired
    private TextDataRepository textDataRepository;

    // すべてのテキストデータを取得
    public List<TextData> getAllTextData() {
        return textDataRepository.findAll();
    }

    // ユーザー ID でテキストデータを取得
    public List<TextData> getTextDataByUserId(Long userId) {
        return textDataRepository.findByUserId(userId);
    }

    // ユーザーのすべての日別集計を取得（タイマー秒数）
    public List<DailyStats> getUserStatsForYear(Long userId) {
        List<TextData> userTextData = getTextDataByUserId(userId);
        
        if (userTextData.isEmpty()) {
            return new ArrayList<>();
        }
        
        // createdAt のタイムスタンプを LocalDate に変換してタイマー秒数を集計
        Map<LocalDate, Long> statsMap = new HashMap<>();
        
        LocalDate minDate = null;
        LocalDate maxDate = null;
        
        // ユーザーのテキストデータのタイマー秒数を集計
        for (TextData data : userTextData) {
            LocalDate date = Instant.ofEpochMilli(data.getCreatedAt())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            long timerSeconds = data.getTimerSeconds() != null ? data.getTimerSeconds() : 0L;
            
            // タイマー秒数が正の値のみ集計
            if (timerSeconds > 0) {
                statsMap.put(date, statsMap.getOrDefault(date, 0L) + timerSeconds);
                
                // 最早日と最新日を追跡
                if (minDate == null || date.isBefore(minDate)) {
                    minDate = date;
                }
                if (maxDate == null || date.isAfter(maxDate)) {
                    maxDate = date;
                }
            }
        }
        
        // データがない場合
        if (minDate == null || maxDate == null) {
            return new ArrayList<>();
        }
        
        // すべての日付を初期化（0 秒）
        for (LocalDate date = minDate; !date.isAfter(maxDate); date = date.plusDays(1)) {
            statsMap.putIfAbsent(date, 0L);
        }
        
        // LocalDate でソートして DailyStats のリストに変換（秒をINTEGER（分）に変換）
        return statsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    // 秒を分に変換（小数点以下は切り上げ）
                    long seconds = entry.getValue();
                    int minutes = Math.toIntExact((seconds + 59) / 60);
                    return new DailyStats(entry.getKey().toString(), minutes);
                })
                .collect(Collectors.toList());
    }

    // ID でテキストデータを取得
    public Optional<TextData> getTextDataById(Long id) {
        return textDataRepository.findById(id);
    }

    // テキストデータを保存
    public TextData saveTextData(TextData textData) {
        return textDataRepository.save(textData);
    }

    // テキストデータを削除
    public void deleteTextData(Long id) {
        textDataRepository.deleteById(id);
    }

    // テキストデータを更新
    public TextData updateTextData(Long id, TextData updatedTextData) {
        Optional<TextData> existingData = textDataRepository.findById(id);
        if (existingData.isPresent()) {
            TextData data = existingData.get();
            // textが送信されている場合は更新
            if (updatedTextData.getText() != null) {
                data.setText(updatedTextData.getText());
            }
            // timerSecondsが送信されている場合は更新
            if (updatedTextData.getTimerSeconds() != null) {
                data.setTimerSeconds(updatedTextData.getTimerSeconds());
            }
            return textDataRepository.save(data);
        }
        return null;
    }

    // ========================================
    // 作業時間管理メソッド
    // ========================================

    /**
     * 作業セッションの時間を保存（日付指定）
     * 
     * 📚 このメソッドの役割：
     * フロントエンドのタイマーウィジェットから送信された作業時間を
     * データベースに保存する。
     * 
     * 💡 処理の流れ：
     * 1. 指定された日付のレコードを検索
     * 2. 存在すれば作業時間を累積（加算）
     * 3. 存在しなければ新規レコードを作成
     * 
     * @param userId       ユーザーID（ログインユーザー）
     * @param dateString   日付（"YYYY-MM-DD" 形式）
     * @param timerSeconds 作業時間（秒単位）
     * @return 保存されたTextDataエンティティ
     */
    public TextData saveWorkSession(Long userId, String dateString, Long timerSeconds) {
        // 日付文字列をLocalDateに変換
        // 📚 LocalDate: 日付のみを扱うJava 8以降のクラス（時間情報なし）
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
        
        // その日の開始時刻をミリ秒で計算
        // 📚 atStartOfDay(): その日の00:00:00を取得
        // 📚 ZoneId.systemDefault(): サーバーのタイムゾーンを使用
        long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // その日の終了時刻をミリ秒で計算
        // 📚 plusDays(1): 翌日の00:00:00 = その日の24:00:00
        long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        // ユーザーのその日のレコードを検索
        // 📚 stream/filter/findFirst: Java 8のStream APIを使った検索
        // 全レコードから条件に合うものを探す
        List<TextData> userDataList = textDataRepository.findByUserId(userId);
        Optional<TextData> existingData = userDataList.stream()
                .filter(data -> {
                    long createdAt = data.getCreatedAt();
                    // 作成日時がその日の範囲内かチェック
                    return createdAt >= startOfDay && createdAt < endOfDay;
                })
                .findFirst();
        
        if (existingData.isPresent()) {
            // ========================================
            // 既存レコードがある場合: 作業時間を累積（加算）
            // ========================================
            TextData data = existingData.get();
            
            // 現在の作業時間を取得（nullの場合は0）
            long currentSeconds = data.getTimerSeconds() != null ? data.getTimerSeconds() : 0L;
            
            // 新しい作業時間を加算
            // 📚 なぜ累積？: 1日に複数回タイマーを使う可能性があるため
            data.setTimerSeconds(currentSeconds + timerSeconds);
            
            // 更新日時を設定
            data.setUpdatedAt(System.currentTimeMillis());
            
            // データベースに保存して返す
            return textDataRepository.save(data);
        } else {
            // ========================================
            // 新規レコードを作成
            // ========================================
            TextData newData = new TextData();
            newData.setUserId(userId);
            
            // テキストには作業記録であることを示す文字列を設定
            newData.setText("作業セッション - " + dateString);
            
            // 作成日時をその日の正午に設定
            // 📚 なぜ正午？: タイムゾーンの境界問題を避けるため
            newData.setCreatedAt(startOfDay + (12 * 60 * 60 * 1000)); // 正午
            newData.setUpdatedAt(System.currentTimeMillis());
            
            // 作業時間を設定
            newData.setTimerSeconds(timerSeconds);
            
            // データベースに保存して返す
            return textDataRepository.save(newData);
        }
    }
}
