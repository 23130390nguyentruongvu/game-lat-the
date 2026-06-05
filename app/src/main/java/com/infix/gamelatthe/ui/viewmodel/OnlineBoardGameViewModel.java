package com.infix.gamelatthe.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
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

    public void onCardClick(CardOnline clickedCard, RoomOnline currentRoom, String currentUserId) {
        if (isProcessing) return;

        // 7.1.1 Hệ thống kiểm tra: ID người chơi phải trùng với currentTurn và thẻ chưa bị lật.
        if (currentRoom.getCurrentTurn() == null || !currentRoom.getCurrentTurn().equals(currentUserId)) {
            // 7.3.1 Thao tác sai lượt.
            // 7.3.2 Chặn thao tác, từ chối gửi Request.
            // 7.3.3 Hiển thị Toast cảnh báo "Chưa tới lượt của bạn!".
            return;
        }

        if (clickedCard.isFlipped() || clickedCard.isMatched()) return;

        if (firstCard == null && secondCard == null) {
            // 7.1.0 Người chơi chạm vào một thẻ đang úp trên giao diện bàn cờ.
            firstCard = clickedCard;
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            // 7.1.2 Hệ thống đẩy lệnh cập nhật lên Firestore (isFlipped = true).
            gameRepository.updateBoardAndTurn(currentRoom);

            // 7.1.3 Trình lắng nghe nhận sự kiện, đồng bộ hoạt họa lật ngửa thẻ.

        } else if (firstCard != null && secondCard == null) {
            secondCard = clickedCard;
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            // 7.1.4 Cập nhật isFlipped = true cho thẻ thứ 2.
            gameRepository.updateBoardAndTurn(currentRoom);

            // 7.1.5 Hệ thống tiến hành kiểm tra quy tắc so khớp.
            isProcessing = true;
            gameRuleEngine = new GameRuleEngine(currentRoom.getBoardGame());

            if (gameRuleEngine.matchTwoCard(firstCard, secondCard)) {
                // 7.1.6 Hai thẻ trùng khớp: Cập nhật isMatched = true và cộng điểm.
                updateCardStateInRoom(currentRoom, firstCard.getId(), true, true);
                updateCardStateInRoom(currentRoom, secondCard.getId(), true, true);

                addScoreForPlayer(currentRoom, currentUserId);

                // 7.1.7 Giữ nguyên lượt đi cho người chơi hiện tại.
                gameRepository.updateBoardAndTurn(currentRoom);

                resetSelection();
                isProcessing = false;

                // 7.1.8 Gọi UC-8 Kiểm tra kết thúc trực tuyến.
                checkEndGameOnline(currentRoom);
            } else {
                // 7.2.1 Hai thẻ không trùng khớp.
                // 7.2.2 Thiết lập bộ đếm thời gian (Delay 1.5s).
                handler.postDelayed(() -> {
                    // 7.2.3 Đẩy lệnh cập nhật lại trạng thái isFlipped = false.
                    updateCardStateInRoom(currentRoom, firstCard.getId(), false, false);
                    updateCardStateInRoom(currentRoom, secondCard.getId(), false, false);

                    // 7.2.4 Cập nhật biến currentTurn sang ID của đối thủ.
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

    public void checkEndGameOnline(RoomOnline currentRoom) {
        if (currentRoom == null) return;
        gameRuleEngine = new GameRuleEngine(currentRoom.getBoardGame());

        // [8.1.2] Nếu lật hết bài
        if (gameRuleEngine.checkOnlineEndGame(currentRoom)) {
            // [8.1.3] Tính người thắng
            String winnerId = gameRuleEngine.calculateOnlineWinner(currentRoom);
            executeEndGameSequence(currentRoom, "FINISHED", winnerId);
        }
    }

    public void abandonGame(String currentUserId, RoomOnline currentRoom) {
        if (currentRoom == null || currentRoom.getPlayers() == null) return;
        String winnerId = "";
        for (PlayerOnline p : currentRoom.getPlayers()) {
            if (!p.getUuid().equals(currentUserId)) {
                winnerId = p.getUuid();
                break;
            }
        }
        executeEndGameSequence(currentRoom, "ABANDONED", winnerId);
    }

    private void executeEndGameSequence(RoomOnline room, String status, String winnerId) {
        gameRepository.endRoomOnline(room.getRoomId(), status, winnerId, new RoomOnlineListener() {
            @Override
            public void onSuccess(String message) {
                gameRepository.stopListeningToRoom(); // [8.1.8] Hủy lắng nghe
                _gameOverEvent.postValue(winnerId);   // [8.1.9] Báo Fragment hiện Dialog
            }

            @Override
            public void onFailure() {
                _networkError.postValue(true); // [8.3] Báo lỗi rớt mạng
            }
        });
    }
}