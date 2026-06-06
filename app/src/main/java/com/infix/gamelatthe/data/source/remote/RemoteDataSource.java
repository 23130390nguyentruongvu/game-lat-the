package com.infix.gamelatthe.data.source.remote;

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
                        String docId = docSnap.getId();

                        RoomOnline roomOnline = docSnap.toObject(RoomOnline.class);
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

                        //6.3.2 Hệ thống tiến hành cập nhật lại trạng thái trên cơ sở dữ liệu Firebase Firestore:
                        if (targetPlayer.getUuid().equals(uuid)) {
                            //	Nếu là Host rời phòng: Hệ thống xóa hoàn toàn document của phòng đó trên Firestore.
                            if (UserRole.HOST.role.equals(targetPlayer.getRole())) {
                                db.collection("rooms")
                                        .document(docId)
                                        .delete()
                                        .addOnSuccessListener(aVoid -> {
                                            roomOnlineListener.onSuccess("Chuyển hướng");
                                        })
                                        .addOnFailureListener(e -> {
                                            roomOnlineListener.onFailure();
                                        });
                            }
                            //	Nếu là Guest rời phòng: Hệ thống xóa thông tin của
                            // Guest ra khỏi document phòng, chuyển trạng thái phòng quay lại
                            // thành "WAITING". Thiết bị của Host nhận được sự kiện cập nhật sẽ ẩn thông
                            // tin Guest đi và tiếp tục chờ người mới.
                            else if (UserRole.GUEST.role.equals(targetPlayer.getRole())) {
                                roomOnline.getPlayers().remove(targetPlayer);

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("players", roomOnline.getPlayers());
                                updates.put("status", StatusRoomOnlineEnum.WAITING.name());

                                db.collection("rooms")
                                        .document(docId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            roomOnlineListener.onSuccess("Chuyển hướng");
                                        })
                                        .addOnFailureListener(e -> {
                                            roomOnlineListener.onFailure();
                                        });
                            } else {
                                roomOnlineListener.onFailure();
                                return;
                            }
                        }


                    } else
                        roomOnlineListener.onFailure();
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
                    String roomId = roomDocSnap.getId();
                    RoomOnline roomOnline = roomDocSnap.toObject(RoomOnline.class);

                    if (roomOnline == null || roomOnline.getPlayers() == null || roomOnline.getPlayers().isEmpty()) {
                        roomOnlineListener.onFailure();
                        return;
                    }
                    //6.1.9 Hệ thống tiến hành khởi tạo ma trận thẻ trực tuyến trong phòng chơi:
                    //Hệ thống (máy Host) truy vấn dữ liệu cấu trúc thẻ bài gốc từ Collection boards/[difficulty]/cards của phiên bản 1.
                    //Thuật toán tại thiết bị Host thực hiện trộn ngẫu nhiên vị trí các thẻ bài vừa lấy về.
                    //Bổ sung thuộc tính trạng thái trực tuyến cho từng thẻ bài: isFlipped = false và isMatched = false.
                    //Đẩy toàn bộ mảng dữ liệu trạng thái bàn chơi (boardState) này lên Document của phòng chơi trên Firestore.
                    //Ngẫu nhiên chọn một người giữ lượt đi trước bằng cách gán ID vào trường currentTurn và đổi trạng thái phòng (status) sang "PLAYING".

                    String difficulty = roomOnline.getDifficulty();

                    db.collection("boards")
                            .document(difficulty.toUpperCase())
                            .collection("cards")
                            .get()
                            .addOnCompleteListener(cardsTask -> {
                                if (!cardsTask.isSuccessful() || cardsTask.getResult() == null || cardsTask.getResult().isEmpty()) {
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

                                List<PlayerOnline> players = roomOnline.getPlayers();
                                Random random = new Random();
                                int randomIndex = random.nextInt(players.size());
                                String firstTurnPlayerId = players.get(randomIndex).getUuid();

                                Map<String, Object> updates = new HashMap<>();

                                if (roomOnline.getBoardGame() != null) {
                                    roomOnline.getBoardGame().setCards(originalCards);
                                    updates.put("boardGame", roomOnline.getBoardGame());
                                } else {
                                    BoardGame boardGameMap = new BoardGame();
                                    boardGameMap.setCards(originalCards);
                                    updates.put("boardGame", boardGameMap);
                                }

                                updates.put("currentTurn", firstTurnPlayerId);
                                updates.put("status", StatusRoomOnlineEnum.PLAYING.name());

                                db.collection("rooms").document(roomId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            roomOnlineListener.onSuccess("Trận đấu bắt đầu!");
                                        })
                                        .addOnFailureListener(e -> {
                                            roomOnlineListener.onFailure();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    roomOnlineListener.onFailure();
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
                    // 10.1.3 Firebase Firestore trả về danh sách các trận đấu tương ứng.
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
