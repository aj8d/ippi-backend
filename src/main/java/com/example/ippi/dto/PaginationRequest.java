package com.example.ippi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PaginationRequest {
    @Min(value = 0, message = "ページ番号は0以上である必要があります")
    private int page = 0;

    @Min(value = 1, message = "ページサイズは1以上である必要があります")
    @Max(value = 100, message = "ページサイズは100以下である必要があります")
    private int size = 20;

    public PaginationRequest() {}

    public PaginationRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
