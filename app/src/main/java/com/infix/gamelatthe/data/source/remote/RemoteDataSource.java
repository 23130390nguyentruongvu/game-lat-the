package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.StatusRoomOnlineEnum;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;

import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class RemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface LevelsCallback {
        void onSuccess(List<String> levels);
        void onError(String error);
    }

    public interface BoardCallback {
        void onSuccess(List<Card> cards);
        void onError(String error);
    }

    // LOAD LEVELS
    public void getLevels(LevelsCallback callback) {
        db.collection("levels")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> levels = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        levels.add(doc.getId());
                    }
                    callback.onSuccess(levels);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // LOAD CARDS
    public void getBoard(DifficultyEnum level, BoardCallback callback) {
        db.collection("boards")
                .document(level.name())
                .collection("cards")
                .get()
                .addOnSuccessListener(query -> {

                    List<Card> cards = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        Card c = doc.toObject(Card.class);
                        cards.add(c);
                    }

                    callback.onSuccess(cards);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createRoomOnline(PlayerOnline hostPlayer, String difficulty, RoomOnlineListener roomOnlineListener) {
        String potentialCode = AppUtils.generateUniqueCode();

        db.collection("rooms")
                .whereEqualTo("roomCode", potentialCode)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (!task.getResult().isEmpty()) {
                            createRoomOnline(hostPlayer, difficulty, roomOnlineListener);
                            return;
                        }

                        CollectionReference roomsRef = db.collection("rooms");
                        String newRoomId = roomsRef.document().getId();

                        List<PlayerOnline> playersList = new ArrayList<>();
                        playersList.add(hostPlayer);

                        RoomOnline newRoom = new RoomOnline();
                        newRoom.setRoomId(newRoomId);
                        newRoom.setRoomCode(potentialCode);
                        newRoom.setStatus(StatusRoomOnlineEnum.WAITING.name());
                        newRoom.setDifficulty(difficulty);
                        newRoom.setPlayers(playersList);
                        newRoom.setBoardGame(
                                new BoardGame(new ArrayList<>(), 0L)
                        );
                        newRoom.setCurrentTurn("");
                        newRoom.setWinnerId("");

                        roomsRef.document(newRoomId).set(newRoom)
                                .addOnSuccessListener(aVoid -> {
                                    roomOnlineListener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    roomOnlineListener.onFailure();
                                });
                    } else {
                        roomOnlineListener.onFailure();
                    }
                });
    }
}