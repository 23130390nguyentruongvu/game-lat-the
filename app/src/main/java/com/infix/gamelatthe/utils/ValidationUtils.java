package com.infix.gamelatthe.utils;

public class ValidationUtils {

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 3;
    }
}