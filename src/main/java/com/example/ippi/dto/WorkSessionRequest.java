package com.example.ippi.dto;

/**
 * 作業セッションの保存リクエストDTO
 */
public class WorkSessionRequest {

    private String date;
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
