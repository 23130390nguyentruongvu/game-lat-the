package com.infix.gamelatthe.data.source.remote;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.common.StatusRoomOnlineEnum;
import com.infix.gamelatthe.common.UserRole;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;

import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.utils.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration roomListenerRegistration;

    public interface LevelsCallback {
        void onSuccess(List<String> levels);
        void onError(String error);
    }

    public interface BoardCallback {
        void onSuccess(List<Card> cards);
        void onError(String error);
    }

    public interface RoomQueryCallback {
        void onRoomsLoaded(List<RoomOnline> rooms);
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
                        if (c != null) cards.add(c);
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
                        newRoom.setBoardGame(new BoardGame(new ArrayList<>(), 0L));
                        newRoom.setCurrentTurn("");
                        newRoom.setWinnerId("");

                        roomsRef.document(newRoomId).set(newRoom)
                                .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess(potentialCode))
                                .addOnFailureListener(e -> roomOnlineListener.onFailure());
                    } else {
                        roomOnlineListener.onFailure();
                    }
                });
    }

    public void enterRoomOnline(PlayerOnline guestPlayer, String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms")
                .whereEqualTo("roomCode", roomCode)
                .whereEqualTo("status", StatusRoomOnlineEnum.WAITING.name())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            roomOnlineListener.onFailure();
                            return;
                        }

                        DocumentSnapshot roomDoc = task.getResult().getDocuments().get(0);
                        RoomOnline currentRoom = roomDoc.toObject(RoomOnline.class);

                        if (currentRoom != null) {
                            List<PlayerOnline> currentPlayers = currentRoom.getPlayers();
                            if (currentPlayers == null) currentPlayers = new ArrayList<>();
                            if (currentPlayers.size() >= 2) {
                                roomOnlineListener.onFailure();
                                return;
                            }

                            currentPlayers.add(guestPlayer);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("players", currentPlayers);

                            db.collection("rooms").document(roomDoc.getId())
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess(roomCode))
                                    .addOnFailureListener(e -> roomOnlineListener.onFailure());
                        } else {
                            roomOnlineListener.onFailure();
                        }
                    } else {
                        roomOnlineListener.onFailure();
                    }
                });
    }

    public void startListeningToRoomByCode(String roomCode, RoomSnapshotCallback callback) {
        stopListening();
        roomListenerRegistration = db.collection("rooms")
                .whereEqualTo("roomCode", roomCode)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        RoomOnline room = parseRoomOnlineManual(snapshot.getDocuments().get(0));
                        if (room != null) {
                            callback.onDataChanged(room);
                        }
                    } else if (snapshot != null && snapshot.isEmpty()) {
                        callback.onDataChanged(null);
                    }
                });
    }

    public void leaveRoomOnline(String uuid, String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms")
                .whereEqualTo("roomCode", roomCode)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        DocumentSnapshot docSnap = task.getResult().getDocuments().get(0);
                        RoomOnline roomOnline = parseRoomOnlineManual(docSnap);
                        
                        if (roomOnline == null || roomOnline.getPlayers() == null) {
                            roomOnlineListener.onFailure();
                            return;
                        }

                        PlayerOnline targetPlayer = null;
                        for (PlayerOnline p : roomOnline.getPlayers()) {
                            if (p.getUuid().equals(uuid)) {
                                targetPlayer = p;
                                break;
                            }
                        }

                        if (targetPlayer == null) {
                            roomOnlineListener.onFailure();
                            return;
                        }

                        if (UserRole.HOST.role.equals(targetPlayer.getRole())) {
                            db.collection("rooms").document(docSnap.getId()).delete()
                                    .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess("Deleted"))
                                    .addOnFailureListener(e -> roomOnlineListener.onFailure());
                        } else {
                            roomOnline.getPlayers().remove(targetPlayer);
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("players", roomOnline.getPlayers());
                            updates.put("status", StatusRoomOnlineEnum.WAITING.name());
                            db.collection("rooms").document(docSnap.getId()).update(updates)
                                    .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess("Left"))
                                    .addOnFailureListener(e -> roomOnlineListener.onFailure());
                        }
                    } else {
                        roomOnlineListener.onFailure();
                    }
                });
    }

    public void startGameOnline(String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms")
                .whereEqualTo("roomCode", roomCode)
                .get()
                .addOnCompleteListener(roomTask -> {
                    if (!roomTask.isSuccessful() || roomTask.getResult() == null || roomTask.getResult().isEmpty()) {
                        roomOnlineListener.onFailure();
                        return;
                    }

                    DocumentSnapshot roomDocSnap = roomTask.getResult().getDocuments().get(0);
                    RoomOnline roomOnline = parseRoomOnlineManual(roomDocSnap);

                    if (roomOnline == null) {
                        roomOnlineListener.onFailure();
                        return;
                    }

                    db.collection("boards")
                            .document(roomOnline.getDifficulty().toUpperCase())
                            .collection("cards")
                            .get()
                            .addOnCompleteListener(cardsTask -> {
                                if (!cardsTask.isSuccessful() || cardsTask.getResult() == null) {
                                    roomOnlineListener.onFailure();
                                    return;
                                }

                                List<Card> originalCards = new ArrayList<>();
                                for (DocumentSnapshot cardDoc : cardsTask.getResult().getDocuments()) {
                                    CardOnline card = cardDoc.toObject(CardOnline.class);
                                    if (card != null) {
                                        card.setFlipped(false);
                                        card.setMatched(false);
                                        originalCards.add(card);
                                    }
                                }
                                Collections.shuffle(originalCards);

                                String firstTurnId = roomOnline.getPlayers().get(new Random().nextInt(roomOnline.getPlayers().size())).getUuid();
                                
                                Map<String, Object> updates = new HashMap<>();
                                BoardGame bg = new BoardGame(originalCards, System.currentTimeMillis());
                                updates.put("boardGame", bg);
                                updates.put("currentTurn", firstTurnId);
                                updates.put("status", StatusRoomOnlineEnum.PLAYING.name());

                                db.collection("rooms").document(roomDocSnap.getId()).update(updates)
                                        .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess("Started"))
                                        .addOnFailureListener(e -> roomOnlineListener.onFailure());
                            });
                });
    }

    public void stopListening() {
        if (roomListenerRegistration != null) {
            roomListenerRegistration.remove();
            roomListenerRegistration = null;
        }
    }

    public void queryRoomsByUserUUID(String userUUID, RoomQueryCallback callback) {
        db.collection("rooms")
                .get()
                .addOnSuccessListener(snapshot -> {
                    // 10.1.3 Firebase Firestore trả về danh sách các trận đấu tương ứng.
                    List<RoomOnline> matchedRooms = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        // QUAN TRỌNG: Dùng hàm parse thủ công để lấy đầy đủ dữ liệu mảng players từ Firestore
                        RoomOnline room = parseRoomOnlineManual(doc);
                        if (room != null && room.getPlayers() != null) {
                            for (PlayerOnline player : room.getPlayers()) {
                                if (player.getUuid() != null && player.getUuid().equals(userUUID)) {
                                    matchedRooms.add(room);
                                    break;
                                }
                            }
                        }
                    }
                    callback.onRoomsLoaded(matchedRooms);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private RoomOnline parseRoomOnlineManual(DocumentSnapshot document) {
        try {
            RoomOnline room = new RoomOnline();
            room.setRoomId(document.getId());
            room.setRoomCode(document.getString("roomCode"));
            room.setStatus(document.getString("status"));
            room.setDifficulty(document.getString("difficulty"));
            room.setCurrentTurn(document.getString("currentTurn"));
            room.setWinnerId(document.getString("winnerId"));
            room.setCreateAt(document.getDate("createAt"));

            List<Map<String, Object>> playersMapList = (List<Map<String, Object>>) document.get("players");
            List<PlayerOnline> playersList = new ArrayList<>();
            if (playersMapList != null) {
                for (Map<String, Object> playerMap : playersMapList) {
                    PlayerOnline player = new PlayerOnline();
                    player.setUuid((String) playerMap.get("uuid"));
                    player.setName((String) playerMap.get("name"));
                    player.setRole((String) playerMap.get("role"));
                    if (playerMap.get("score") != null) player.setScore(((Number) playerMap.get("score")).intValue());
                    player.setReady(Boolean.TRUE.equals(playerMap.get("ready")));
                    playersList.add(player);
                }
            }
            room.setPlayers(playersList);

            Map<String, Object> boardGameMap = (Map<String, Object>) document.get("boardGame");
            if (boardGameMap != null) {
                BoardGame boardGame = new BoardGame();
                boardGame.setTimeInit(boardGameMap.get("timeInit") != null ? ((Number) boardGameMap.get("timeInit")).longValue() : 0L);
                boardGame.setTimeEnd(boardGameMap.get("timeEnd") != null ? ((Number) boardGameMap.get("timeEnd")).longValue() : 0L);

                List<Map<String, Object>> cardsMapList = (List<Map<String, Object>>) boardGameMap.get("cards");
                List<Card> cards = new ArrayList<>();
                if (cardsMapList != null) {
                    for (Map<String, Object> cardMap : cardsMapList) {
                        CardOnline card = new CardOnline();
                        if (cardMap.get("id") != null) card.setId(((Number) cardMap.get("id")).intValue());
                        if (cardMap.get("groupId") != null) card.setGroupId(((Number) cardMap.get("groupId")).intValue());
                        card.setUrlImage((String) cardMap.get("urlImage"));
                        card.setFlipped(Boolean.TRUE.equals(cardMap.get("flipped")));
                        card.setEnable(Boolean.TRUE.equals(cardMap.get("enable")));
                        card.setMatched(Boolean.TRUE.equals(cardMap.get("matched")));
                        cards.add(card);
                    }
                }
                boardGame.setCards(cards);
                room.setBoardGame(boardGame);
            }
            return room;
        } catch (Exception e) {
            Log.e("RemoteDataSource", "Error parsing room: " + e.getMessage());
            return null;
        }
    }
}
