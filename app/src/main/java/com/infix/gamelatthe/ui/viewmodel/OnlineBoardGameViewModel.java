package com.infix.gamelatthe.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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

    // Đã fix chuẩn kiến trúc MVVM: _ (Mutable) dùng để ghi, không _ (LiveData) dùng để đọc
    private final MutableLiveData<String> _gameOverEvent = new MutableLiveData<>();
    public LiveData<String> gameOverEvent = _gameOverEvent;

    private final MutableLiveData<Boolean> _networkError = new MutableLiveData<>();
    public LiveData<Boolean> networkError = _networkError;


    private final MutableLiveData<RoomOnline> _roomOnline = new MutableLiveData<>();
    public LiveData<RoomOnline> roomOnline = _roomOnline;
    private final MatchHistoryRepository matchHistoryRepository = new MatchHistoryRepository();
    public void onCardClick(CardOnline clickedCard, RoomOnline currentRoom, String currentUserId) {
        if (isProcessing) return;

        // 7.1.1 Hệ thống kiểm tra: ID người chơi phải trùng với currentTurn và thẻ chưa bị lật.
        if (currentRoom.getCurrentTurn() == null || !currentRoom.getCurrentTurn().equals(currentUserId)) {
            // 7.3.1 Thao tác sai lượt.
            // 7.3.2 Chặn thao tác, từ chối gửi Request.
            // 7.3.3 Hiển thị Toast cảnh báo "Chưa tới lượt của bạn!".
            return;
        }

        if (clickedCard.isFlipped() || clickedCard.isMatched()) {return;}

        if (firstCard == null && secondCard == null) {
            // 7.1.0 Người chơi chạm vào một thẻ đang úp trên giao diện bàn cờ.
            firstCard = clickedCard;
           // updateFirstCardInRoom(currentRoom, clickedCard.getId(), true);
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            // 7.1.2 Hệ thống đẩy lệnh cập nhật lên Firestore (isFlipped = true).
            gameRepository.updateBoardAndTurn(currentRoom);

            // 7.1.3 Trình lắng nghe nhận sự kiện, đồng bộ hoạt họa lật ngửa thẻ.

        } else if (firstCard != null && secondCard == null) {
            secondCard = clickedCard;
            updateCardStateInRoom(currentRoom, clickedCard.getId(), true, false);

            isProcessing = true;

            // 7.1.4 Cập nhật isFlipped = true cho thẻ thứ 2.
            gameRepository.updateBoardAndTurn(currentRoom);

            // 7.1.5 Hệ thống tiến hành kiểm tra quy tắc so khớp.
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

    private void updateFirstCardInRoom(RoomOnline currentRoom, int cardId, boolean isFlipped) {
        currentRoom.getBoardGame().setCardIsFlipped(cardId, isFlipped);
    }


    public void startListeningToRoomByCode(String roomCode) {
        gameRepository.startListeningToRoomByCode(roomCode, new RoomSnapshotCallback() {
            @Override
            public void onDataChanged(RoomOnline room) {
                _roomOnline.setValue(room);
            }

            @Override
            public void onError(Exception e) {

            }
        });
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

    public void setRoomOnline(RoomOnline room) {
        _roomOnline.setValue(room);
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

    /* * 8.1.1: Hệ thống (OnlineBoardGameViewModel) tự động kích hoạt hàm checkGameEndCondition()
     * để bắt đầu quy trình kiểm tra điều kiện kết thúc ván.
     */
    public void checkEndGameOnline(RoomOnline currentRoom) {
        if (currentRoom == null) return;
        gameRuleEngine = new GameRuleEngine(currentRoom.getBoardGame());

        if (gameRuleEngine.checkOnlineEndGame(currentRoom)) {
            // 8.1.5: ViewModel tự kích hoạt hàm nội bộ triggerEndGame(winnerId) để mở tiến trình đóng ván game.
            processEndGameResult(currentRoom);
        }
    }

    public void processEndGameResult(RoomOnline currentRoom) {
        if (currentRoom == null) return;

        /* * 8.1.3: ViewModel tiếp tục yêu cầu GameRuleEngine xử lý hàm calculateWinner(host, guest).
         */
        GameRuleEngine engine = new GameRuleEngine(currentRoom.getBoardGame());
        String winnerId = engine.calculateOnlineWinner(currentRoom);

        /* * 8.1.6: ViewModel gọi xuống GameRepository, truyền lệnh qua RemoteDataSource để
         * thực thi hàm updateRoomStatusAndWinner(roomId, "FINISHED", winnerId).
         * 8.1.7: RemoteDataSource thực hiện truy vấn ghi đè dữ liệu lên Firebase Firestore...
         */
        gameRepository.endRoomOnline(currentRoom.getRoomId(), "FINISHED", winnerId, new RoomOnlineListener() {
            @Override
            public void onSuccess(String message) {
                /* * 8.1.8: Sau khi database cập nhật thành công, ViewModel gọi hàm removeRoomListener()
                 * để ngắt bộ lắng nghe thời gian thực (Snapshot Listener) nhằm giải phóng tài nguyên.
                 */
                gameRepository.stopListeningToRoom();

                // ======= UC-9 ADD START =======

                String opponentName = "";
                int score = 0;
                String role = "";

                for (PlayerOnline p : currentRoom.getPlayers()) {
                    if (p.getUuid().equals(winnerId)) {
                        role = p.getRole();
                        score = p.getScore();
                    }
                }

                MatchHistoryItem history = new MatchHistoryItem(
                        currentRoom.getRoomId(),
                        currentRoom.getDifficulty(),
                        role,
                        opponentName,
                        winnerId.equals(currentRoom.getWinnerId()) ? "WIN" : "LOSE",
                        score,
                        (currentRoom.getBoardGame().getTimeEnd()
                                - currentRoom.getBoardGame().getTimeInit()) / 1000,
                        new java.util.Date()
                );

                matchHistoryRepository.saveMatchHistory(history, new MatchHistoryRepository.Callback() {
                    @Override
                    public void onSuccess() {
                        _gameOverEvent.postValue(winnerId);
                    }

                    @Override
                    public void onError(Exception e) {
                        _networkError.postValue(true);
                    }
                });

                // ======= UC-9 ADD END =======
            }

            @Override
            public void onFailure() {
                /*
                 * 8.3.1 -> 8.3.4: (Luồng Exceptions) Bắt được ngoại lệ lỗi kết nối mạng.
                 * Ghi gói thông tin vào bộ nhớ đệm (Local Cache của Firebase) và báo lỗi ra UI.
                 */
                _networkError.postValue(true);
            }
        });
    }

    /*
     * 8.2.3: Hệ thống thực hiện gửi lệnh cập nhật trạng thái của người chơi thoát trận thành "Bỏ cuộc"
     */
    public void abandonGame(String currentUserId, RoomOnline currentRoom) {
        if (currentRoom == null || currentRoom.getPlayers() == null || currentRoom.getPlayers().isEmpty()) {
            return;
        }

        String opponentId = currentUserId;

        /*
         * 8.2.4: ViewModel lập tức hủy bỏ các tiến trình kiểm tra thông thường, tự động lấy
         * mã uuid của người chơi còn lại duy nhất ở trong phòng để gán vào làm winnerId.
         */
        for (PlayerOnline player : currentRoom.getPlayers()) {
            if (player != null && player.getUuid() != null && !player.getUuid().equals(currentUserId)) {
                opponentId = player.getUuid();
                break;
            }
        }

        final String finalOpponentId = opponentId;

        /*
         * 8.2.5: ViewModel yêu cầu GameRepository gửi lệnh cập nhật kết quả xuống RemoteDataSource.
         * 8.2.6: RemoteDataSource thực hiện hàm để ghi nhận trạng thái phòng thành "ABANDONED"...
         */
        gameRepository.endRoomOnline(currentRoom.getRoomId(), "ABANDONED", finalOpponentId, new RoomOnlineListener() {
            @Override
            public void onSuccess(String message) {
                gameRepository.stopListeningToRoom(); // Hủy lắng nghe dữ liệu ngầm để tránh rò rỉ bộ nhớ

                // FIX LỖI 2 & 3: Dùng _gameOverEvent và biến finalOpponentId
                _gameOverEvent.postValue(finalOpponentId);
            }

            @Override
            public void onFailure() {
                // Luồng lỗi mạng 8.3
                _networkError.postValue(true);
            }
        });
    }
}