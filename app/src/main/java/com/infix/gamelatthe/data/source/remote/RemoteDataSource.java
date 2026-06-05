package com.infix.gamelatthe.data.source.remote;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.common.StatusRoomOnlineEnum;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;

import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.utils.AppUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Đổi tên từ LeaderboardCallback thành RoomQueryCallback để khớp với đặc tả
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
                                    roomOnlineListener.onSuccess(potentialCode);
                                })
                                .addOnFailureListener(e -> {
                                    roomOnlineListener.onFailure();
                                });
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
                    //-	Mã phòng không tồn tại: (Rẽ nhánh từ bước 6.2.3 khi hệ thống kiểm tra mã phòng từ Firebase)
                    //6.4.1 Hệ thống quét cơ sở dữ liệu Firestore và phát hiện ra một trong các sự cố sau:
                    //	Mã phòng nhập vào không tồn tại trên hệ thống dữ liệu.
                    //	Phòng đấu tương ứng đã đủ 2 người chơi (Trạng thái phòng khác "WAITING").
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            roomOnlineListener.onFailure();
                            return;
                        }

                        DocumentSnapshot roomDoc = task.getResult().getDocuments().get(0);
                        String roomId = roomDoc.getId();

                        RoomOnline currentRoom = roomDoc.toObject(RoomOnline.class);

                        if (currentRoom != null) {
                            List<PlayerOnline> currentPlayers = currentRoom.getPlayers();
                            if (currentPlayers == null) {
                                currentPlayers = new ArrayList<>();
                            }

                            if (currentPlayers.size() >= 2) {
                                roomOnlineListener.onFailure();
                                return;
                            }

                            currentPlayers.add(guestPlayer);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("players", currentPlayers);
//                             updates.put("status", "PLAYING");

                            db.collection("rooms").document(roomId)
                                    .update(updates)
                                    .addOnSuccessListener(aVoid -> {
//                                        currentRoom.setPlayers(currentPlayer);

                                        roomOnlineListener.onSuccess(roomCode);
                                    })
                                    .addOnFailureListener(e -> {
                                        roomOnlineListener.onFailure();
                                    });
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
                        RoomOnline room = snapshot.getDocuments().get(0).toObject(RoomOnline.class);
                        if (room != null) {
                            callback.onDataChanged(room);
                        }
                    }
                });
    }

    public void stopListening() {
        if (roomListenerRegistration != null) {
            roomListenerRegistration.remove();
            roomListenerRegistration = null;
        }
    }
    // Cập nhật để sử dụng RoomQueryCallback
    public void queryRoomsByUserUUID(String userUUID, RoomQueryCallback callback) {
        db.collection("rooms")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<RoomOnline> matchedRooms = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RoomOnline room = doc.toObject(RoomOnline.class);
                        if (room != null && room.getPlayers() != null) {
                            for (PlayerOnline player : room.getPlayers()) {
                                if (player.getUuid().equals(userUUID)) {
                                    matchedRooms.add(room);
                                    break;
                                }
                            }
                        }
                    }

                    callback.onRoomsLoaded(matchedRooms); // Đổi onSuccess thành onRoomsLoaded
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage())); // Đổi onFailure thành onError
    }
}
