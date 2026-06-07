package com.infix.gamelatthe.data.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MatchHistoryRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveMatchHistory(MatchHistoryItem item, Callback callback) {
        Map<String, Object> data = new HashMap<>();
        // [QUAN TRỌNG] Lưu userUUID để UC10 có thể lọc đúng lịch sử của bạn
        data.put("userUUID", item.userUUID);
        data.put("roomId", item.roomId);
        data.put("difficulty", item.difficulty);
        data.put("role", item.role);
        data.put("opponentName", item.opponentName);
        data.put("result", item.result);
        data.put("score", item.score);
        data.put("playTime", item.playTime);
        data.put("createAt", new Date());

        db.collection("match_history")
                .add(data)
                .addOnSuccessListener(doc -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e));
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
}