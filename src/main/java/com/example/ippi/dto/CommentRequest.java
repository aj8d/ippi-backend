package com.example.ippi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "コメントは必須です")
    @Size(max = 500, message = "コメントは500文字以内で入力してください")
    private String text;

    public CommentRequest() {}

    public CommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
