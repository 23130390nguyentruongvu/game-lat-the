package com.infix.gamelatthe.ui.viewmodel.multi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
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


    public void setRoomOnlineState(RoomOnline roomOnline) {
        _roomData.setValue(roomOnline);
        if (roomOnline != null && roomOnline.getBoardGame() != null) {
            this.ruleEngine = new GameRuleEngine(roomOnline.getBoardGame());
        }
    }

   /*  // [8.1.1] Hệ thống tự động kích hoạt kiểm tra điều kiện đóng ván đấu khi có thẻ được lật trúng
    public void checkGameEndCondition(RoomOnline room) {
        if (room == null || ruleEngine == null) return;

        // [8.1.2] Gọi GameRuleEngine để kiểm tra toàn bộ thẻ đã lật (isFlipped == true)
        if (ruleEngine.checkOnlineEndGame(room)) {

            // [8.1.3] & [8.1.4] Tính toán tìm ra mã người thắng cuộc (winnerId)
            String winnerId = ruleEngine.calculateOnlineWinner(room);


        }
    }  */

   public void checkGameEndCondition(RoomOnline room) {

       // 9.1.1 ViewModel nhận dữ liệu trạng thái trận đấu từ View
       if(room == null || ruleEngine == null){
           return;
       }

       // 9.1.2 ViewModel kiểm tra điều kiện kết thúc trận đấu
       if(ruleEngine.checkOnlineEndGame(room)){

           // 9.1.3 ViewModel xác định người thắng cuộc
           String winnerId =
                   ruleEngine.calculateOnlineWinner(room);

           // 9.1.4 ViewModel tạo dữ liệu lịch sử trận đấu
           room.setWinnerId(winnerId);
           room.setStatus("FINISHED");

           if(room.getBoardGame() != null){
               room.getBoardGame().setTimeEnd(
                       System.currentTimeMillis()
               );
           }

           // 9.1.5 ViewModel yêu cầu Repository lưu lịch sử trận đấu
           repository.finishGameOnline(
                   room,
                   new RoomOnlineListener() {

                       @Override
                       public void onSuccess(String message) {

                           // 9.1.8 ViewModel nhận kết quả lưu thành công
                           _gameOverEvent.postValue(winnerId);
                       }

                       @Override
                       public void onFailure() {

                           // 9.2.2 Hệ thống thông báo lỗi lưu lịch sử trận đấu
                           _networkError.postValue(true);
                       }
                   }
           );
       }
   }
}