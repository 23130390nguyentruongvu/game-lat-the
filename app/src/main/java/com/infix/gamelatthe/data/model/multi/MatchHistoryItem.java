package com.infix.gamelatthe.data.model.multi;

import java.util.Date;public class MatchHistoryItem {
    public String userUUID;      // ID người chơi để UC10 tìm kiếm
    public String roomId;
    public String difficulty;
    public String role;         // HOST hoặc GUEST
    public String opponentName;
    public String result;       // WIN hoặc LOSE
    public int score;
    public long playTime;       // tính bằng giây
    public Date createAt;

    // Constructor rỗng bắt buộc để Firebase có thể đọc dữ liệu
    public MatchHistoryItem() {}

    // Constructor đầy đủ 9 tham số
    public MatchHistoryItem(String userUUID, String roomId, String difficulty, String role, String opponentName, String result, int score, long playTime, Date createAt) {
        this.userUUID = userUUID;
        this.roomId = roomId;
        this.difficulty = difficulty;
        this.role = role;
        this.opponentName = opponentName;
        this.result = result;
        this.score = score;
        this.playTime = playTime;
        this.createAt = createAt;
    }
}
