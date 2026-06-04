package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.data.model.multi.RoomOnline;

public class GamePlayOnlineDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void updateBoardAndTurn(RoomOnline roomOnline) {
        if (roomOnline == null || roomOnline.getRoomId() == null) return;

        db.collection("rooms")
                .document(roomOnline.getRoomId())
                .set(roomOnline);
    }
}