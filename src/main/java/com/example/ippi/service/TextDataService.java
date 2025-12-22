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

    // ユーザーの過去 365 日間の日別集計を取得（タイマー秒数）
    public List<DailyStats> getUserStatsForYear(Long userId) {
        List<TextData> userTextData = getTextDataByUserId(userId);
        
        // 現在の日付から過去 365 日間を計算
        LocalDate today = LocalDate.now();
        LocalDate oneYearAgo = today.minusDays(364);
        
        // createdAt のタイムスタンプを LocalDate に変換してタイマー秒数を集計
        Map<LocalDate, Long> statsMap = new HashMap<>();
        
        // 過去 365 日間のすべての日付を初期化（0 秒）
        for (LocalDate date = oneYearAgo; !date.isAfter(today); date = date.plusDays(1)) {
            statsMap.put(date, 0L);
        }
        
        // ユーザーのテキストデータのタイマー秒数を集計
        for (TextData data : userTextData) {
            LocalDate date = Instant.ofEpochMilli(data.getCreatedAt())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            // 過去 365 日以内のデータのみ集計
            if (!date.isBefore(oneYearAgo) && !date.isAfter(today)) {
                long timerSeconds = data.getTimerSeconds() != null ? data.getTimerSeconds() : 0L;
                statsMap.put(date, statsMap.get(date) + timerSeconds);
            }
        }
        
        // LocalDate でソートして DailyStats のリストに変換（秒をINTEGER（分）に変換）
        return statsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int minutes = Math.toIntExact(entry.getValue() / 60);
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
