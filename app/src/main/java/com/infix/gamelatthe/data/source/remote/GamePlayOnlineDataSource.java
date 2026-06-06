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

     // UC-9
    public void updateMatchHistory(
            RoomOnline roomOnline,
            RoomOnlineListener listener
    ) {

        if (roomOnline == null
                || roomOnline.getRoomId() == null) {

            listener.onFailure();
            return;
        }

        Map<String, Object> updates =
                new HashMap<>();

        // 9.1.4 Cập nhật người thắng
        updates.put(
                "winnerId",
                roomOnline.getWinnerId()
        );

        // 9.1.5 Cập nhật trạng thái phòng
        updates.put(
                "status",
                roomOnline.getStatus()
        );

        // 9.1.6 Cập nhật thời gian kết thúc
        updates.put(
                "boardGame.timeEnd",
                roomOnline.getBoardGame()
                        .getTimeEnd()
        );

        db.collection("rooms")
                .document(
                        roomOnline.getRoomId()
                )
                .update(updates)
                .addOnSuccessListener(
                        unused ->
                                listener.onSuccess(
                                        "Success"
                                )
                )
                .addOnFailureListener(
                        e -> listener.onFailure()
                );
    }

}
