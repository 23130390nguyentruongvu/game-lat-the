package com.infix.gamelatthe.data.model.multi;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PlayerOnline)) return false;
        PlayerOnline that = (PlayerOnline) o;
        return getScore() == that.getScore() && isReady() == that.isReady() && Objects.equals(getUuid(), that.getUuid()) && Objects.equals(getName(), that.getName()) && Objects.equals(getRole(), that.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid(), getName(), getScore(), isReady(), getRole());
    }
}
