package com.example.ippi.util;

import java.util.Arrays;

public class FileValidationUtil {

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] GIF87_MAGIC = new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61};
    private static final byte[] GIF89_MAGIC = new byte[]{0x47, 0x49, 0x46, 0x38, 0x39, 0x61};
    private static final byte[] WEBP_RIFF = new byte[]{0x52, 0x49, 0x46, 0x46};
    private static final byte[] WEBP_WEBP = new byte[]{0x57, 0x45, 0x42, 0x50};

    public static boolean isValidImage(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 12) {
            return false;
        }

        if (startsWith(fileBytes, JPEG_MAGIC)) {
            return true;
        }

        if (startsWith(fileBytes, PNG_MAGIC)) {
            return true;
        }

        if (startsWith(fileBytes, GIF87_MAGIC) || startsWith(fileBytes, GIF89_MAGIC)) {
            return true;
        }

        if (startsWith(fileBytes, WEBP_RIFF) && containsAt(fileBytes, WEBP_WEBP, 8)) {
            return true;
        }

        return false;
    }

    public static String detectImageType(byte[] fileBytes) {
        if (fileBytes == null || fileBytes.length < 12) {
            return null;
        }

        if (startsWith(fileBytes, JPEG_MAGIC)) {
            return "image/jpeg";
        }

        if (startsWith(fileBytes, PNG_MAGIC)) {
            return "image/png";
        }

        if (startsWith(fileBytes, GIF87_MAGIC) || startsWith(fileBytes, GIF89_MAGIC)) {
            return "image/gif";
        }

        if (startsWith(fileBytes, WEBP_RIFF) && containsAt(fileBytes, WEBP_WEBP, 8)) {
            return "image/webp";
        }

        return null;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        return Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }

    private static boolean containsAt(byte[] data, byte[] pattern, int offset) {
        if (data.length < offset + pattern.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (data[offset + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }
}
