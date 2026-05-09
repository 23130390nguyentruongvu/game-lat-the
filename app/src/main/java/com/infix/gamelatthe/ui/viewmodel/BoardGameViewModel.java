package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.TrackStateFlipTwoCard;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.PlayHistory; // Import thêm Entity này
import com.infix.gamelatthe.data.repository.HistoryRepository;
import com.infix.gamelatthe.ui.GameRuleEngine;

public class BoardGameViewModel extends ViewModel {
    private final MutableLiveData<TrackStateFlipTwoCard> _stateFlipTwoCard = new MutableLiveData<>();
    public LiveData<TrackStateFlipTwoCard> stateFlipTwoCard = _stateFlipTwoCard;

    private final MutableLiveData<String> _notifyMessage = new MutableLiveData<>();
    public LiveData<String> notifyMessage = _notifyMessage;

    private final MutableLiveData<String> _errorEvent = new MutableLiveData<>();
    public LiveData<String> errorEvent = _errorEvent;

    private Card firstCard, secondCard;
    private GameRuleEngine gameRuleEngine;
    private HistoryRepository repository;

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
        return true;
    }
}