package com.example.ippi.util;

import java.security.SecureRandom;

public class IdGenerator {
    private static final String SYSTEM_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SYSTEM_ID_LENGTH = 16;
    private static final SecureRandom random = new SecureRandom();

    /**
     * 複雑なシステムID を生成（大文字小文字混在、数字を含む16文字）
     */
    public static String generateSystemId() {
        StringBuilder sb = new StringBuilder(SYSTEM_ID_LENGTH);
        for (int i = 0; i < SYSTEM_ID_LENGTH; i++) {
            sb.append(SYSTEM_ID_CHARS.charAt(random.nextInt(SYSTEM_ID_CHARS.length())));
        }
        return sb.toString();
    }
}
