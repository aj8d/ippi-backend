package com.example.ippi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class WorkSessionRequest {

    @NotBlank(message = "日付は必須です")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "日付はyyyy-MM-dd形式で入力してください")
    private String date;

    @NotNull(message = "作業時間は必須です")
    @Min(value = 1, message = "作業時間は1秒以上である必要があります")
    private Long timerSeconds;
    
    public WorkSessionRequest() {}
    public WorkSessionRequest(String date, Long timerSeconds) {
        this.date = date;
        this.timerSeconds = timerSeconds;
    }
    

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    
    public Long getTimerSeconds() {
        return timerSeconds;
    }
    public void setTimerSeconds(Long timerSeconds) {
        this.timerSeconds = timerSeconds;
    }
}
