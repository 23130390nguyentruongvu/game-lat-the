package com.infix.gamelatthe.common;

public enum UserRole {
    HOST("HOST"),
    GUEST("GUEST");

    public String role;

    UserRole(String role) {
        this.role = role;
    }
}
