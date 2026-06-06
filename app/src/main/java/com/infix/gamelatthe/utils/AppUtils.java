package com.infix.gamelatthe.utils;

import java.util.UUID;

public class AppUtils {
    public static String generateUniqueCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return uuid.substring(0, 6);
    }
}
