package com.infix.gamelatthe.ui.viewmodel;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.StateFlipTwoCard;
import com.infix.gamelatthe.common.TrackStateFlipTwoCard;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.GameConfig;
import com.infix.gamelatthe.data.repository.HistoryRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

import java.util.List;
import android.os.Handler;

public class BoardGameViewModel extends ViewModel {
    private final Handler handler;
    private final MutableLiveData<TrackStateFlipTwoCard> _stateFlipTwoCard = new MutableLiveData<>();
    public LiveData<TrackStateFlipTwoCard> stateFlipTwoCard = _stateFlipTwoCard;

    private final MutableLiveData<String> _notifyMessage = new MutableLiveData<>();
    public LiveData<String> notifyMessage = _notifyMessage;

    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;

    private Card firstCard, secondCard;
    private GameConfig gameConfig;
    private GameRuleEngine gameRuleEngine;
    private HistoryRepository repository = new HistoryRepository();

    public BoardGameViewModel() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void onCardClick(Card card) {
        //2.1.3 ViewModel kiểm tra trạng thái lựa chọn hiện tại
        checkSelectionState(card);
    }

    public Card getFirstCard() {
        return firstCard;
    }

    public Card getSecondCard() {
        return secondCard;
    }

    public List<Card> getCardsOfBoard() {
        return gameRuleEngine.getCards();
    }

    public void setGameConfig(GameConfig gameConfig) {
        if(gameConfig == null) return;
        this.gameConfig = gameConfig;
    }

    //Thiết lập đối tượng GameRuleEngine mới
    public void setBoardGame(BoardGame boardGame) {
        if(boardGame == null) return;
        this.gameRuleEngine = new GameRuleEngine(boardGame);
    }

    private void checkSelectionState(Card card) {
        //2.1.4 Nếu chưa có thẻ nào được chọn:  Ghi nhận thẻ hiện tại là thẻ thứ nhất
        if(firstCard == null && secondCard == null) {
           setFirstCard(card);
            //2.1.6 ViewModel cập nhật trạng thái lật thẻ
            updateStateFlipCard(new TrackStateFlipTwoCard(StateFlipTwoCard.FLIP_UP_NOW, true));
            return;
        }
        //2.1.5 Nếu đã có thẻ thứ nhất: Ghi nhận thẻ hiện tại là thẻ thứ hai
        if (firstCard != null && secondCard == null) {
            //-	(Rẽ nhánh từ 2.1.5) – Nếu người chơi chọn lại thẻ đầu tiên đã lật
            if(card.getId() == firstCard.getId()) {
                //2.3.1 ViewModel cập nhật trạng thái lỗi
                _notifyMessage.setValue("Không được chọn lại thẻ đã lật");
                //2.3.3 Quay lại bước 2.1.1
                return;
            }

            setSecondCard(card);
            //2.1.6 ViewModel cập nhật trạng thái lật thẻ
            updateStateFlipCard(new TrackStateFlipTwoCard(StateFlipTwoCard.FLIP_UP_NOW, false));
        }else
            return;

        //2.1.8 Nếu đã chọn đủ hai thẻ: ViewModel gọi GameRuleEngine để kiểm tra hai thẻ
        boolean isMatch = gameRuleEngine.matchTwoCard(firstCard, secondCard);

        //2.1.9 Nếu hai thẻ khớp: Thông báo trạng thái khớp thẻ
        if(isMatch) {
            _stateFlipTwoCard.setValue(new TrackStateFlipTwoCard(StateFlipTwoCard.DISABLE_TWO_CARD_NOW, true));
        }
        //-	(Rẽ nhánh từ 2.1.9) – Nếu hai thẻ không khớp
        else {
            //2.2.1 ViewModel cập nhật trạng thái không khớp
            _stateFlipTwoCard.setValue(new TrackStateFlipTwoCard(StateFlipTwoCard.NOT_MATCH, true));

            //2.2.3 ViewModel thiết lập bộ đếm thời gian 1s để xử lý tiếp theo
            handler.postDelayed( () -> {
                //2.2.4 Sau 1s, Cập nhật trạng thái úp ngược 2 thẻ
                _stateFlipTwoCard.setValue(new TrackStateFlipTwoCard(StateFlipTwoCard.FLIP_DOWN_NOW, true));
                //2.2.6 Reset trạng thái lựa chọn thẻ
                resetSelection();
                //2.2.7 Kích hoạt sự kiện UC-3 (Kiểm tra kết thúc và hiển thị kết quả)
                boolean isEnd = gameRuleEngine.checkEndGame();

                //2.2.8 Nếu chưa kết thúc, quay lại bước 2.1.1
                if(!isEnd) {
                    return;
                }
            }, 1000);

            return;
        }

        //2.1.11 Kích hoạt UC-3 (Kiểm tra kết thúc ván)
        boolean isEnd = gameRuleEngine.checkEndGame();
        //2.1.12 Nếu chưa kết thúc:
        //    - Reset trạng thái lựa chọn
        //    - Cho phép người chơi tiếp tục từ bước 2.1.1
        if(!isEnd) {
            resetSelection();
        }
    }
    // 3.1.1 và 3.1.2 View Model gọi GameRuleEngine để kiểm tra trạng thái ván chơi
    public void checkEndGame(){
        //3.1.3 GameRuleEngie kiểm tra trạng thái tất cả các thẻ
        if(gameRuleEngine.checkEndGame()){
            // 3.1.5 Tính toán thời gian hoàn thành
            long finishTime = gameRuleEngine.calcTimeFinish();
            // 3.1.7 Kích hoạt UC4
            onGameEnded(

            )
            //3.1.8 Cập nhật thông báo kết thúc
            _notifyMessage.setValue("Game Finished");
        }
        else{
            handleGameNotFinished();
        }
    }

    // UC3.2.3: Cập nhật trạng thái tiếp tục trò chơi
    private void handleGameNotFinished() {
        _notifyMessage.setValue("Continue Game");
        // UC3.2.4: View sẽ quan sát và giữ nguyên giao diện bàn chơi
        // UC3.2.5: ViewModel tiếp tục chờ tương tác từ người chơi (UC-2)
    }


    private void updateStateFlipCard(TrackStateFlipTwoCard state) {
        _stateFlipTwoCard.setValue(state);
    }

    private void setFirstCard(Card card) {
        if(card == null)
            return;
        firstCard = card;
    }

    private void setSecondCard(Card card) {
        if(card == null)
            return;
        secondCard = card;
    }

    private void resetSelection() {
        firstCard = null;
        secondCard = null;
    }
}
