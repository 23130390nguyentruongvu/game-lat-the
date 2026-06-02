package com.infix.gamelatthe.data.model.multi;

import com.infix.gamelatthe.common.UserRole;

public class PlayerOnline {
    private String uuid, name;
    private int score;
    private boolean isReady;
    private String role;

    public PlayerOnline() {
    }

    public PlayerOnline(String uuid, String name, int score, boolean isReady, String role) {
        this.uuid = uuid;
        this.name = name;
        this.score = score;
        this.isReady = isReady;
        this.role = role;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public boolean isReady() {
        return isReady;
    }

    public String getRole() {
        return role;
    }
}
