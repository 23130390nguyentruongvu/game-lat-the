package com.infix.gamelatthe.common;

public enum DifficultyEnum {
    EASY, NORMAL, HARD;

    public static DifficultyEnum fromString(String value) {
        if (value == null) return EASY;
        return value.toUpperCase().equals("HARD") ? HARD :
                value.toUpperCase().equals("NORMAL") ? NORMAL : EASY;
    }
}
