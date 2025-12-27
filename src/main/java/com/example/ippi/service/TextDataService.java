package com.example.ippi.service;

import com.example.ippi.entity.TextData;
import com.example.ippi.dto.DailyStats;
import com.example.ippi.repository.TextDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextDataService {

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
}
