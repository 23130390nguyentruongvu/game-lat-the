package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.RoomOnline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePlayOnlineDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void updateBoardAndTurn(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getRoomId() == null || roomOnline.getBoardGame() == null) return;

        try {
            List<Map<String, Object>> cardsMapList = new ArrayList<>();

            for (com.infix.gamelatthe.data.model.Card cardAbstract : roomOnline.getBoardGame().getCards()) {
                Map<String, Object> cardMap = new HashMap<>();
                cardMap.put("id", cardAbstract.getId());
                cardMap.put("groupId", cardAbstract.getGroupId());
                cardMap.put("urlImage", cardAbstract.getUrlImage());
                cardMap.put("flipped", cardAbstract.isFlipped());
                cardMap.put("enable", cardAbstract.isEnable());

                if (cardAbstract instanceof com.infix.gamelatthe.data.model.multi.CardOnline) {
                    com.infix.gamelatthe.data.model.multi.CardOnline cardOnline = (com.infix.gamelatthe.data.model.multi.CardOnline) cardAbstract;
                    cardMap.put("matched", cardOnline.isMatched());
                } else {
                    cardMap.put("matched", false);
                }
                cardsMapList.add(cardMap);
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("currentTurn", roomOnline.getCurrentTurn());
            updates.put("status", roomOnline.getStatus());
            updates.put("winnerId", roomOnline.getWinnerId());
            updates.put("boardGame.cards", cardsMapList);

            db.collection("rooms")
                    .document(roomOnline.getRoomId())
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
