package com.infix.gamelatthe.ui.viewmodel.multi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

public class OnlineBoardGameViewModel extends ViewModel {
    private final GameRepository repository = new GameRepository();
    private GameRuleEngine ruleEngine;

    private final MutableLiveData<RoomOnline> _roomData = new MutableLiveData<>();
    public LiveData<RoomOnline> roomData = _roomData;

    private final MutableLiveData<String> _gameOverEvent = new MutableLiveData<>();
    public LiveData<String> gameOverEvent = _gameOverEvent;

    private final MutableLiveData<Boolean> _networkError = new MutableLiveData<>();
    public LiveData<Boolean> networkError = _networkError;

    // UC-9
    private final MutableLiveData<Boolean> _saveHistorySuccess = new MutableLiveData<>();
    public LiveData<Boolean> saveHistorySuccess = _saveHistorySuccess;

    public void setRoomOnlineState(RoomOnline roomOnline) {
        _roomData.setValue(roomOnline);
        if (roomOnline != null && roomOnline.getBoardGame() != null) {
            this.ruleEngine = new GameRuleEngine(roomOnline.getBoardGame());
        }
    }

    // [8.1.1] Hệ thống tự động kích hoạt kiểm tra điều kiện đóng ván đấu khi có thẻ được lật trúng
    public void checkGameEndCondition(RoomOnline room) {
        if (room == null || ruleEngine == null) return;

        // [8.1.2] Gọi GameRuleEngine để kiểm tra toàn bộ thẻ đã lật (isFlipped == true)
        if (ruleEngine.checkOnlineEndGame(room)) {

            // [8.1.3] & [8.1.4] Tính toán tìm ra mã người thắng cuộc (winnerId)
            String winnerId = ruleEngine.calculateOnlineWinner(room);

            // UC-08 truyền dữ liệu sang UC-09
            updateMatchHistory(room, winnerId);

        }
    }

    public void updateMatchHistory(
            RoomOnline roomOnline,
            String winnerId
    ) {

        // 9.1.1 ViewModel nhận dữ liệu kết quả từ UC-08
        if (roomOnline == null) {
            return;
        }

        // 9.1.2 Cập nhật dữ liệu kết quả trận đấu
        roomOnline.setWinnerId(winnerId);
        roomOnline.setStatus("FINISHED");

        if (roomOnline.getBoardGame() != null) {
            roomOnline.getBoardGame()
                    .setTimeEnd(System.currentTimeMillis());
        }

        // 9.1.3 ViewModel gửi yêu cầu cập nhật lịch sử trận đấu
        repository.updateMatchHistory(roomOnline,  new RoomOnlineListener(){

                    @Override
                    public void onSuccess(String message) {

                        // 9.1.7 Nhận kết quả cập nhật thành công
                        _saveHistorySuccess.postValue(true);

                        _gameOverEvent.postValue(winnerId);
                    }

                    @Override
                    public void onFailure() {

                        // 9.2.3 Nhận kết quả cập nhật thất bại
                        _networkError.postValue(true);
                    }
                }
        );
    }
}