package com.example.ippi.dto;

/**
 * WorkSessionRequest - 作業セッションの保存リクエストDTO
 * 
 * 📚 このクラスの役割：
 * フロントエンドのタイマーウィジェットから送信される
 * 作業完了時のデータを受け取るためのデータ転送オブジェクト（DTO）
 * 
 * 💡 DTOとは：
 * Data Transfer Objectの略。
 * クライアント（フロントエンド）とサーバー（バックエンド）間で
 * データをやり取りするための「入れ物」クラス。
 * エンティティ（データベースのテーブル）とは別に定義することで、
 * 必要なデータだけを安全にやり取りできる。
 * 
 * 📝 使用例：
 * フロントエンドから以下のようなJSONが送信される：
 * {
 *   "date": "2024-12-31",
 *   "timerSeconds": 1500
 * }
 */
public class WorkSessionRequest {
    
    /**
     * 作業を行った日付
     * 形式: "YYYY-MM-DD"（例: "2024-12-31"）
     * 
     * 📚 なぜ日付が必要？
     * 統計表示のために、どの日に何分作業したかを記録する必要がある。
     * フロントエンドから送信される日付を使用することで、
     * タイムゾーンの問題を避けることができる。
     */
    private String date;
    
    /**
     * 作業時間（秒単位）
     * 
     * 📚 なぜ秒単位？
     * より正確な記録が可能。
     * 表示時には分や時間に変換する。
     * 例: 1500秒 = 25分（1ポモドーロ）
     */
    private Long timerSeconds;
    
    // ========================================
    // コンストラクタ
    // ========================================
    
    /**
     * デフォルトコンストラクタ
     * 
     * 📚 なぜ必要？
     * Spring FrameworkがJSONをJavaオブジェクトに変換（デシリアライズ）する際に
     * 引数なしのコンストラクタが必要。
     * これがないとエラーになる。
     */
    public WorkSessionRequest() {}
    
    /**
     * 全引数コンストラクタ
     * テスト時などに便利
     */
    public WorkSessionRequest(String date, Long timerSeconds) {
        this.date = date;
        this.timerSeconds = timerSeconds;
    }
    
    // ========================================
    // Getter/Setter
    // ========================================
    
    /**
     * 日付を取得
     * 
     * 📚 Getterとは？
     * privateフィールドの値を外部から取得するためのメソッド。
     * カプセル化（データを隠蔽して保護する）の原則に基づく。
     */
    public String getDate() {
        return date;
    }
    
    /**
     * 日付を設定
     * 
     * 📚 Setterとは？
     * privateフィールドの値を外部から設定するためのメソッド。
     * SpringがJSONからオブジェクトを作成する際に使用する。
     */
    public void setDate(String date) {
        this.date = date;
    }
    
    /**
     * 作業秒数を取得
     */
    public Long getTimerSeconds() {
        return timerSeconds;
    }
    
    /**
     * 作業秒数を設定
     */
    public void setTimerSeconds(Long timerSeconds) {
        this.timerSeconds = timerSeconds;
    }
}
