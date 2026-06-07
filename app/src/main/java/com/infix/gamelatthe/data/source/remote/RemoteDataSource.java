package com.infix.gamelatthe.data.source.remote;

import android.util.Log;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.common.StatusRoomOnlineEnum;
import com.infix.gamelatthe.common.UserRole;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
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

    // [10.1.2] Định nghĩa Interface để trả về lịch sử đấu cho UC10
    public interface HistoryQueryCallback {
        void onHistoryLoaded(List<MatchHistoryItem> history);
        void onError(String error);
    }

    public void getLevels(LevelsCallback callback) {
        db.collection("levels").get().addOnSuccessListener(query -> {
            List<String> levels = new ArrayList<>();
            for (DocumentSnapshot doc : query) levels.add(doc.getId());
            callback.onSuccess(levels);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getBoard(DifficultyEnum level, BoardCallback callback) {
        db.collection("boards").document(level.name()).collection("cards").get().addOnSuccessListener(query -> {
            List<Card> cards = new ArrayList<>();
            for (DocumentSnapshot doc : query) {
                Card c = doc.toObject(Card.class);
                if (c != null) cards.add(c);
            }
            callback.onSuccess(cards);
        }).addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void createRoomOnline(PlayerOnline hostPlayer, String difficulty, RoomOnlineListener roomOnlineListener) {
        String potentialCode = AppUtils.generateUniqueCode();
        db.collection("rooms").whereEqualTo("roomCode", potentialCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (!task.getResult().isEmpty()) {
                    createRoomOnline(hostPlayer, difficulty, roomOnlineListener);
                    return;
                }
                String newRoomId = db.collection("rooms").document().getId();
                List<PlayerOnline> playersList = new ArrayList<>();
                playersList.add(hostPlayer);
                RoomOnline newRoom = new RoomOnline(newRoomId, potentialCode, StatusRoomOnlineEnum.WAITING.name(), difficulty, "", "", null, playersList, new BoardGame(new ArrayList<>(), 0L));
                db.collection("rooms").document(newRoomId).set(newRoom)
                        .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess(potentialCode))
                        .addOnFailureListener(e -> roomOnlineListener.onFailure());
            } else {
                roomOnlineListener.onFailure();
            }
        });
    }

    public void enterRoomOnline(PlayerOnline guestPlayer, String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms").whereEqualTo("roomCode", roomCode).whereEqualTo("status", StatusRoomOnlineEnum.WAITING.name()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                DocumentSnapshot roomDoc = task.getResult().getDocuments().get(0);
                RoomOnline currentRoom = roomDoc.toObject(RoomOnline.class);
                if (currentRoom != null) {
                    List<PlayerOnline> currentPlayers = new ArrayList<>(currentRoom.getPlayers());
                    if (currentPlayers.size() >= 2) {
                        roomOnlineListener.onFailure();
                        return;
                    }
                    currentPlayers.add(guestPlayer);
                    db.collection("rooms").document(roomDoc.getId()).update("players", currentPlayers)
                            .addOnSuccessListener(aVoid -> roomOnlineListener.onSuccess(roomCode))
                            .addOnFailureListener(e -> roomOnlineListener.onFailure());
                }
            } else {
                roomOnlineListener.onFailure();
            }
        });
    }

    public void startListeningToRoomByCode(String roomCode, RoomSnapshotCallback callback) {
        stopListening();
        roomListenerRegistration = db.collection("rooms").whereEqualTo("roomCode", roomCode)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        RoomOnline room = parseRoomOnlineManual(snapshot.getDocuments().get(0));
                        if (room != null) callback.onDataChanged(room);
                    }
                });
    }

    public void stopListening() {
        if (roomListenerRegistration != null) {
            roomListenerRegistration.remove();
            roomListenerRegistration = null;
        }
    }

    // [10.1.2] Gửi yêu cầu truy vấn lịch sử thi đấu của người chơi theo userUUID
    public void queryMatchHistoryByUserUUID(String userUUID, HistoryQueryCallback callback) {
        db.collection("match_history")
                .whereEqualTo("userUUID", userUUID)
                .orderBy("createAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // [10.1.3] Firebase Firestore trả về danh sách lịch sử tương ứng
                    List<MatchHistoryItem> history = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        MatchHistoryItem item = doc.toObject(MatchHistoryItem.class);
                        if (item != null) history.add(item);
                    }
                    callback.onHistoryLoaded(history);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void startGameOnline(String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms").whereEqualTo("roomCode", roomCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                RoomOnline room = parseRoomOnlineManual(doc);
                if (room == null) { roomOnlineListener.onFailure(); return; }
                //6.1.9 Hệ thống tiến hành khởi tạo ma trận thẻ trực tuyến trong phòng chơi:
                //Hệ thống (máy Host) truy vấn dữ liệu cấu trúc thẻ bài gốc từ Collection boards/[difficulty]/cards của phiên bản 1.
                //Thuật toán tại thiết bị Host thực hiện trộn ngẫu nhiên vị trí các thẻ bài vừa lấy về.
                //Bổ sung thuộc tính trạng thái trực tuyến cho từng thẻ bài: isFlipped = false và isMatched = false.
                //Đẩy toàn bộ mảng dữ liệu trạng thái bàn chơi (boardState) này lên Document của phòng chơi trên Firestore.
                //Ngẫu nhiên chọn một người giữ lượt đi trước bằng cách gán ID vào trường currentTurn và đổi trạng thái phòng (status) sang "PLAYING".

                db.collection("boards").document(room.getDifficulty().toUpperCase()).collection("cards").get().addOnCompleteListener(cardTask -> {
                    if (cardTask.isSuccessful() && cardTask.getResult() != null) {
                        List<Card> cards = new ArrayList<>();
                        for (DocumentSnapshot cDoc : cardTask.getResult()) {
                            CardOnline co = cDoc.toObject(CardOnline.class);
                            if (co != null) { co.setFlipped(false); co.setMatched(false); cards.add(co); }
                        }
                        Collections.shuffle(cards);
                        String firstTurn = room.getPlayers().get(new Random().nextInt(room.getPlayers().size())).getUuid();
                        db.collection("rooms").document(doc.getId()).update("boardGame", new BoardGame(cards, System.currentTimeMillis()), "currentTurn", firstTurn, "status", StatusRoomOnlineEnum.PLAYING.name())
                                .addOnSuccessListener(v -> roomOnlineListener.onSuccess("Started"))
                                .addOnFailureListener(e -> roomOnlineListener.onFailure());
                    }
                });
            }
        });
    }

    public void leaveRoomOnline(String uuid, String roomCode, RoomOnlineListener roomOnlineListener) {
        db.collection("rooms").whereEqualTo("roomCode", roomCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                DocumentSnapshot docSnap = task.getResult().getDocuments().get(0);
                RoomOnline roomOnline = parseRoomOnlineManual(docSnap);
                if (roomOnline == null || roomOnline.getPlayers() == null) { roomOnlineListener.onFailure(); return; }
                PlayerOnline target = null;
                for (PlayerOnline p : roomOnline.getPlayers()) if (p.getUuid().equals(uuid)) target = p;
                if (target == null) { roomOnlineListener.onFailure(); return; }
                //6.3.2 Hệ thống tiến hành cập nhật lại trạng thái trên cơ sở dữ liệu Firebase Firestore:
                if (UserRole.HOST.role.equals(target.getRole())) {
                    db.collection("rooms").document(docSnap.getId()).delete().addOnSuccessListener(v -> roomOnlineListener.onSuccess("Deleted")).addOnFailureListener(e -> roomOnlineListener.onFailure());
                } else {
                    roomOnline.getPlayers().remove(target);
                    db.collection("rooms").document(docSnap.getId()).update("players", roomOnline.getPlayers(), "status", StatusRoomOnlineEnum.WAITING.name()).addOnSuccessListener(v -> roomOnlineListener.onSuccess("Left")).addOnFailureListener(e -> roomOnlineListener.onFailure());
                }
            }
        });
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
            List<Map<String, Object>> pMaps = (List<Map<String, Object>>) document.get("players");
            List<PlayerOnline> pList = new ArrayList<>();
            if (pMaps != null) {
                for (Map<String, Object> m : pMaps) {
                    PlayerOnline p = new PlayerOnline((String) m.get("uuid"), (String) m.get("name"), m.get("score") != null ? ((Number) m.get("score")).intValue() : 0, Boolean.TRUE.equals(m.get("ready")), (String) m.get("role"));
                    pList.add(p);
                }
            }
            room.setPlayers(pList);
            Map<String, Object> bgMap = (Map<String, Object>) document.get("boardGame");
            if (bgMap != null) {
                BoardGame bg = new BoardGame();
                bg.setTimeInit(bgMap.get("timeInit") != null ? ((Number) bgMap.get("timeInit")).longValue() : 0L);
                bg.setTimeEnd(bgMap.get("timeEnd") != null ? ((Number) bgMap.get("timeEnd")).longValue() : 0L);
                List<Map<String, Object>> cMaps = (List<Map<String, Object>>) bgMap.get("cards");
                List<Card> cList = new ArrayList<>();
                if (cMaps != null) {
                    for (Map<String, Object> cm : cMaps) {
                        CardOnline co = new CardOnline(cm.get("id") != null ? ((Number) cm.get("id")).intValue() : 0, cm.get("groupId") != null ? ((Number) cm.get("groupId")).intValue() : 0, (String) cm.get("urlImage"), Boolean.TRUE.equals(cm.get("flipped")));
                        co.setMatched(Boolean.TRUE.equals(cm.get("matched")));
                        cList.add(co);
                    }
                }
                bg.setCards(cList);
                room.setBoardGame(bg);
            }
            return room;
        } catch (Exception e) { return null; }
    }
}