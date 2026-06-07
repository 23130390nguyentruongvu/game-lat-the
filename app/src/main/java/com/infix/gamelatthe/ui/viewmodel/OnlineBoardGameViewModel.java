package com.infix.gamelatthe.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.data.repository.MatchHistoryRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

import java.util.List;

public class OnlineBoardGameViewModel extends ViewModel {
    private final GameRepository gameRepository = new GameRepository();
    private GameRuleEngine gameRuleEngine;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private CardOnline firstCard = null;
    private CardOnline secondCard = null;
    private boolean isProcessing = false;

    private final MutableLiveData<String> _gameOverEvent = new MutableLiveData<>();
    public LiveData<String> gameOverEvent = _gameOverEvent;

    private final MutableLiveData<Boolean> _networkError = new MutableLiveData<>();
    public LiveData<Boolean> networkError = _networkError;

    private final MutableLiveData<RoomOnline> _roomOnline = new MutableLiveData<>();
    public LiveData<RoomOnline> roomOnline = _roomOnline;

    private final MatchHistoryRepository matchHistoryRepository = new MatchHistoryRepository();

    private String currentUserId;
    private boolean isHistorySaved = false;

    // QUAN TRỌNG: Cần thiết lập ID người chơi để Listener biết lưu cho ai
    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void startListeningToRoomByCode(String roomCode) {
        gameRepository.startListeningToRoomByCode(roomCode, new RoomSnapshotCallback() {
            @Override
            public void onDataChanged(RoomOnline room) {
                _roomOnline.setValue(room);

                // FIX LỖI: Cả 2 máy đều lắng nghe trạng thái phòng.
                // Khi thấy trạng thái kết thúc, mỗi máy tự động lưu lịch sử cho chính mình.
                if (room != null && room.getStatus() != null && !isHistorySaved) {
                    if (room.getStatus().equals("FINISHED") || room.getStatus().equals("ABANDONED")) {
                        if (currentUserId != null && !currentUserId.isEmpty()) {
                            isHistorySaved = true; // Đảm bảo chỉ lưu 1 lần duy nhất
                            saveMatchHistoryToFirestore(room, currentUserId, room.getWinnerId());
                        }
                    }
                }
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    public void onCardClick(CardOnline clickedCard, RoomOnline currentRoom, String currentUserId) {
        this.currentUserId = currentUserId;
        if (isProcessing) return;

        // [7.1.1] Hệ thống (ViewModel) kiểm tra: ID người chơi phải trùng với currentTurn và thẻ chưa bị lật (isFlipped = false).
        if (currentRoom.getCurrentTurn() == null || !currentRoom.getCurrentTurn().equals(currentUserId)) {
            // Alternative Flow 2 <Thao tác sai lượt>
            // [7.3.1] Hệ thống phát hiện ID người dùng thao tác không khớp với biến currentTurn.
            // [7.3.2] Hệ thống chặn thao tác vật lý, từ chối gửi Request lên máy chủ.
            return;
        }

        if (clickedCard.isFlipped() || clickedCard.isMatched()) return;

        if (firstCard == null && secondCard == null) {
            // [7.1.0] Người chơi chạm vào một thẻ đang úp trên giao diện bàn cờ.
            firstCard = clickedCard;
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            // [7.1.2] (Trường hợp lật thẻ LẦN 1): Hệ thống đẩy lệnh cập nhật lên Firestore, chỉ thay đổi trường isFlipped = true.
            gameRepository.updateBoardAndTurn(currentRoom);

        } else if (firstCard != null && secondCard == null) {
            secondCard = clickedCard;
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            // [7.1.4] (Trường hợp lật thẻ LẦN 2): Hệ thống ngay lập tức khóa giao diện tạm thời (chống spam click), cập nhật isFlipped = true lên Firestore.
            isProcessing = true;
            gameRepository.updateBoardAndTurn(currentRoom);

            // [7.1.5] Hệ thống tiến hành kiểm tra quy tắc so khớp (Match Logic). Xác định hai thẻ.
            gameRuleEngine = new GameRuleEngine(currentRoom.getBoardGame());

            if (gameRuleEngine.matchTwoCard(firstCard, secondCard)) {
                // [7.1.6] Hệ thống đẩy lệnh Atomic Update lên Firestore: (1) Cập nhật isMatched = true; (2) Cộng 1 điểm vào score.
                updateCardStateInRoom(currentRoom, firstCard.getId(), true, true);
                updateCardStateInRoom(currentRoom, secondCard.getId(), true, true);
                addScoreForPlayer(currentRoom, currentUserId);

                // [7.1.7] Hệ thống giữ nguyên lượt đi (currentTurn) cho người chơi hiện tại.
                gameRepository.updateBoardAndTurn(currentRoom);

                resetSelection();
                isProcessing = false;

                // [7.1.8] Hệ thống (UI) tự động gửi sự kiện để kích hoạt UC-8 (Kiểm tra kết thúc trực tuyến).
                checkEndGameOnline(currentRoom, currentUserId);
            } else {
                // Alternative Flow 1 <Không khớp thẻ>
                // [7.2.1] Hệ thống xác định hai thẻ không trùng khớp.
                // [7.2.2] Hệ thống thiết lập bộ đếm thời gian, giữ nguyên trạng thái ngửa của hai thẻ trong 1.5 giây.
                handler.postDelayed(() -> {
                    // [7.2.3] Sau 1.5 giây, hệ thống đẩy lệnh lên Firestore để cập nhật lại trạng thái hai thẻ về isFlipped = false.
                    updateCardStateInRoom(currentRoom, firstCard.getId(), false, false);
                    updateCardStateInRoom(currentRoom, secondCard.getId(), false, false);

                    // [7.2.4] Hệ thống cập nhật biến currentTurn trên Firestore sang ID của đối thủ để chuyển giao lượt đi.
                    switchTurn(currentRoom);
                    gameRepository.updateBoardAndTurn(currentRoom);

                    resetSelection();
                    isProcessing = false;
                }, 1500);
            }
        }
    }

    private void updateCardStateInRoom(RoomOnline room, int cardId, boolean isFlipped, boolean isMatched) {
        if (room.getBoardGame() == null || room.getBoardGame().getCards() == null) return;
        List<Card> cards = room.getBoardGame().getCards();
        for (Card c : cards) {
            if (c.getId() == cardId) {
                c.setFlipped(isFlipped);
                if (c instanceof CardOnline) {
                    ((CardOnline) c).setMatched(isMatched);
                }
                break;
            }
        }
    }

    private void addScoreForPlayer(RoomOnline room, String currentUserId) {
        if (room.getPlayers() == null) return;
        for (PlayerOnline p : room.getPlayers()) {
            if (p.getUuid().equals(currentUserId)) {
                p.setScore(p.getScore() + 1);
                break;
            }
        }
    }

    private void switchTurn(RoomOnline room) {
        if (room.getPlayers() == null) return;
        for (PlayerOnline p : room.getPlayers()) {
            if (!p.getUuid().equals(room.getCurrentTurn())) {
                room.setCurrentTurn(p.getUuid());
                break;
            }
        }
    }

    private void resetSelection() {
        firstCard = null;
        secondCard = null;
    }

    public void checkEndGameOnline(RoomOnline currentRoom, String currentUserId) {
        if (currentRoom == null) return;
        gameRuleEngine = new GameRuleEngine(currentRoom.getBoardGame());
        if (gameRuleEngine.checkOnlineEndGame(currentRoom)) {
            processEndGameResult(currentRoom, currentUserId);
        }
    }

    public void processEndGameResult(RoomOnline currentRoom, String currentUserId) {
        if (currentRoom == null) return;
        GameRuleEngine engine = new GameRuleEngine(currentRoom.getBoardGame());
        String winnerId = engine.calculateOnlineWinner(currentRoom);

        // Chỉ cần cập nhật trạng thái phòng lên Firebase.
        // Khi Firebase thay đổi, Listener trên CẢ 2 máy sẽ tự động chạy hàm lưu lịch sử.
        gameRepository.endRoomOnline(currentRoom.getRoomId(), "FINISHED", winnerId, new RoomOnlineListener() {
            @Override
            public void onSuccess(String message) {}
            @Override
            public void onFailure() {
                _networkError.postValue(true);
            }
        });
    }

    public void abandonGame(String currentUserId, RoomOnline currentRoom) {
        if (currentRoom == null || currentRoom.getPlayers() == null || currentRoom.getPlayers().isEmpty()) return;

        String opponentId = "";
        for (PlayerOnline player : currentRoom.getPlayers()) {
            if (!player.getUuid().equals(currentUserId)) {
                opponentId = player.getUuid();
                break;
            }
        }
        final String winnerId = opponentId;

        gameRepository.endRoomOnline(currentRoom.getRoomId(), "ABANDONED", winnerId, new RoomOnlineListener() {
            @Override
            public void onSuccess(String message) {}
            @Override
            public void onFailure() {
                _networkError.postValue(true);
            }
        });
    }

    private void saveMatchHistoryToFirestore(RoomOnline currentRoom, String currentUserId, String winnerId) {
        String opponentName = "Đối thủ";
        int myScore = 0;
        String myRole = "GUEST";

        for (PlayerOnline p : currentRoom.getPlayers()) {
            if (p.getUuid().equals(currentUserId)) {
                myScore = p.getScore();
                // 10.1.4 Hệ thống xác định vai trò của người chơi (HOST hoặc GUEST)
                myRole = p.getRole();
            } else {
                // 10.1.5 Hệ thống xác định đối thủ
                opponentName = p.getName();
            }
        }

        // 10.1.5 Hệ thống xác định kết quả trận đấu (WIN / LOSE / DRAW)
        String resultStr = "DRAW";
        if (winnerId.equals(currentUserId)) resultStr = "WIN";
        else if (!winnerId.equals("DRAW")) resultStr = "LOSE";

        // 10.1.5 Hệ thống xác định thời gian chơi và độ khó
        long playTime = 0;
        if (currentRoom.getBoardGame().getTimeEnd() > 0) {
            playTime = (currentRoom.getBoardGame().getTimeEnd() - currentRoom.getBoardGame().getTimeInit()) / 1000;
        }

        MatchHistoryItem history = new MatchHistoryItem(
                currentUserId,
                currentRoom.getRoomId(),
                currentRoom.getDifficulty(),
                myRole,
                opponentName,
                resultStr,
                myScore,
                playTime,
                new java.util.Date()
        );

        matchHistoryRepository.saveMatchHistory(history, new MatchHistoryRepository.Callback() {
            @Override
            public void onSuccess() {
                gameRepository.stopListeningToRoom(); // Ngắt kết nối sau khi đã hoàn tất lưu
                _gameOverEvent.postValue(winnerId);
            }
            @Override
            public void onError(Exception e) {
                gameRepository.stopListeningToRoom();
                _gameOverEvent.postValue(winnerId);
            }
        });
    }
}