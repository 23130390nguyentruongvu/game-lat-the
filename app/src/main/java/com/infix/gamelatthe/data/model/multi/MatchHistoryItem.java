package com.infix.gamelatthe.data.model.multi;

import java.util.Date;

public class MatchHistoryItem {
    private String roomId;
    private String roomCode;
    private String difficulty;
    private String role;
    private String opponentName;
    private String opponentUUID;
    private String result;
    private int score;
    private int opponentScore;
    private Date createAt;
    private Long playDuration;

    public MatchHistoryItem() {
    }

    public MatchHistoryItem(String roomId, String roomCode, String difficulty,
                            String role, String opponentName, String opponentUUID,
                            String result, int score, int opponentScore,
                            Date createAt, Long playDuration) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.difficulty = difficulty;
        this.role = role;
        this.opponentName = opponentName;
        this.opponentUUID = opponentUUID;
        this.result = result;
        this.score = score;
        this.opponentScore = opponentScore;
        this.createAt = createAt;
        this.playDuration = playDuration;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getRole() {
        return role;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public String getOpponentUUID() {
        return opponentUUID;
    }

    public String getResult() {
        return result;
    }

    public int getScore() {
        return score;
    }

    public int getOpponentScore() {
        return opponentScore;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public Long getPlayDuration() {
        return playDuration;
    }

    // ==================== SETTERS ====================
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public void setOpponentUUID(String opponentUUID) {
        this.opponentUUID = opponentUUID;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setOpponentScore(int opponentScore) {
        this.opponentScore = opponentScore;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public void setPlayDuration(Long playDuration) {
        this.playDuration = playDuration;
    }
}