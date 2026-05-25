package com.infix.gamelatthe.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.StateFlipTwoCard;
import com.infix.gamelatthe.common.TrackStateFlipTwoCard;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.GameConfig;
import com.infix.gamelatthe.data.model.PlayHistory;
import com.infix.gamelatthe.data.repository.HistoryRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

import java.util.List;

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
    private HistoryRepository repository ;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    public void setGameConfig(GameConfig config) {
        if (config == null) return;
        this.gameConfig = config;
    }

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


    //Thiết lập đối tượng GameRuleEngine mới
    public void setBoardGame(BoardGame boardGame) {
        if(boardGame == null) return;
        this.gameRuleEngine = new GameRuleEngine(boardGame);
    }

    public void resetAllState() {
        _error.setValue(null);
        _notifyMessage.setValue(null);
        _errorEvent.setValue(null);
        _stateFlipTwoCard.setValue(null);
        gameRuleEngine = null;
        secondCard = null;
        firstCard =  null;
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
                boolean isEnd = checkEndGame();

                //2.2.8 Nếu chưa kết thúc, quay lại bước 2.1.1
                if(!isEnd) {
                    return;
                }
            }, 1000);

            return;
        }

        //2.1.11 Kích hoạt UC-3 (Kiểm tra kết thúc ván)
        boolean isEnd = checkEndGame();
        //2.1.12 Nếu chưa kết thúc:
        //    - Reset trạng thái lựa chọn
        //    - Cho phép người chơi tiếp tục từ bước 2.1.1
        if(!isEnd) {
            resetSelection();
        }
    }
    // 3.1.1 và 3.1.2 View Model gọi GameRuleEngine để kiểm tra trạng thái ván chơi
    private boolean checkEndGame() {
        boolean isFinished = gameRuleEngine.checkEndGame();

        if (isFinished) {
            // 3.1.5 Ghi nhận thời gian kết thúc (BoardGame sẽ lưu)
            long currentTime = System.currentTimeMillis();
            gameRuleEngine.trackEndTime(currentTime);

            // 3.1.7 Kích hoạt UC-4: Gọi onGameEnded() để lưu kết quả
            onGameEnded(gameConfig.getPlayerName(), gameConfig.getDifficulty().toString(),
                    gameRuleEngine.getBoardGame().getTimeInit(), gameRuleEngine.getBoardGame().getTimeEnd());

            // 3.1.8 ViewModel cập nhật thông báo kết thúc
            long second = ( gameRuleEngine.getBoardGame().getTimeEnd()-gameRuleEngine.getBoardGame().getTimeInit())/1000;
            _notifyMessage.setValue("Hoàn thành ván game với thời gian chơi là " + second + "s");
            return true;
        } else {
            return false;
        }
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

    // Bước 4.1.1: Nhận sự kiện kết thúc ván chơi từ UC-3
    public void onGameEnded(String playerName, String diff, long initTime, long endTime) {

        // Bước 4.1.2: Tạo timestamp và đóng gói vào Entity
        PlayHistory entity = mapToEntity(playerName, diff, initTime, endTime);

        // Bước 4.1.3: Thực hiện validate dữ liệu
        boolean isValid = validateData(entity);

        if (isValid) {
            if (repository != null) {
                // Bước 4.1.4: Gọi Repository để saveResult
                repository.saveResult(entity, new HistoryRepository.RepoCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        // Bước 4.3.2: Phát lỗi LiveData do ngoại lệ bộ nhớ/database
                        _errorEvent.postValue("Lỗi bộ nhớ: Không thể lưu kết quả ván chơi!");
                    }
                });
            }
        } else {
            // Bước 4.2.1: Phát hiện dữ liệu lỗi
            // Bước 4.2.2: Cập nhật State để UI hiển thị thông báo
            _errorEvent.postValue("Dữ liệu lỗi: Thông tin ván chơi không hợp lệ!");
        }
    }

    private PlayHistory mapToEntity(String playerName, String diff, long initTime, long endTime) {
        return new PlayHistory(playerName, diff, initTime, endTime);
    }

    private boolean validateData(PlayHistory entity) {

        if (entity.playerName == null || entity.playerName.trim().isEmpty()) return false;


        if (entity.difficulty == null || entity.difficulty.trim().isEmpty()) return false;


        if (entity.endTime <= entity.initTime) return false;

        if (gameConfig != null && gameConfig.getDifficulty() != null) {
            String validDifficulty = gameConfig.getDifficulty().toString();

            if (!entity.difficulty.equals(validDifficulty)) {
                return false;
            }
        }

        return true;
    }
    public void setRepository(HistoryRepository repo) {
        this.repository = repo;
    }
}