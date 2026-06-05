package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.RoomOnline;

import java.util.HashMap;
import java.util.Map;

public class GamePlayOnlineDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void updateBoardAndTurn(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getRoomId() == null) return;

        db.collection("rooms")
                .document(roomOnline.getRoomId())
                .set(roomOnline);
    }
    public void endRoomOnline(String roomId, String finalStatus, String winnerId, RoomOnlineListener roomOnlineListener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", finalStatus); // "FINISHED" hoặc "ABANDONED"
        updates.put("winnerId", winnerId);

        db.collection("rooms").document(roomId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    roomOnlineListener.onSuccess("Trận đấu kết thúc!");
                })
                .addOnFailureListener(e -> {
                    roomOnlineListener.onFailure();
                });
    }

    public void finishGameOnline(RoomOnline room, RoomOnlineListener listener){
        // 9.1.7 Hệ thống lưu dữ liệu lịch sử trận đấu lên Firestore
        firestore.collection("rooms")
                .document(room.getRoomId())
                .set(room)
                .addOnSuccessListener(unused ->
                        listener.onSuccess("Success"))
                .addOnFailureListener(e ->
                        listener.onFailure());
    }
}