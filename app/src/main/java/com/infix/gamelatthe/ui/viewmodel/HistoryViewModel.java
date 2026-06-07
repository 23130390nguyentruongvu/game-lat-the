package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.infix.gamelatthe.common.UIState;
import com.infix.gamelatthe.data.model.PlayHistory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.infix.gamelatthe.data.source.local.PlayHistoryDao;
public class HistoryViewModel extends ViewModel {

    private PlayHistoryDao playHistoryDao;

    public final MutableLiveData<UIState> _uiState = new MutableLiveData<>();
    public final MutableLiveData<List<PlayHistory>> _historyList = new MutableLiveData<>();
    public final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void setDao(PlayHistoryDao dao) {
        this.playHistoryDao = dao;
    }

    public void getTop10(String difficulty) {
        if (playHistoryDao == null) {
            _errorMessage.setValue("Lỗi: Chưa khởi tạo Database DAO");
            _uiState.setValue(UIState.ERROR);
            return;
        }

        _uiState.setValue(UIState.LOADING);

        executorService.execute(() -> {
            try {
                // 5.1.8 ViewModel gọi Repository/Source để lấy top 10 theo cấp độ
                List<PlayHistory> list = playHistoryDao.getTop10ByDifficulty(difficulty);

                if (list == null || list.isEmpty()) {
                    // RẼ NHÁNH TỪ 5.1.9: Không có dữ liệu lịch sử
                    // 5.2.1 ViewModel trả về danh sách rỗng (Thông qua việc view không nhận được list mới, hoặc set list rỗng nếu cần)
                    // 5.2.2 ViewModel cập nhật lại trạng thái dữ liệu mà View đang quan sát (EMPTY)
                    _uiState.postValue(UIState.EMPTY);
                } else {
                    // 5.1.9 ViewModel trả về danh sách top 10 cho View
                    _historyList.postValue(list);
                    _uiState.postValue(UIState.SUCCESS);
                }
            } catch (Exception e) {
                // RẼ NHÁNH TỪ 5.1.9 (Exceptions): Lỗi lấy dữ liệu
                // 5.3.1 Nhận lỗi từ Repository/Source (Biến 'e')
                // 5.3.2 ViewModel cập nhật lại trạng thái lỗi
                _errorMessage.postValue(e.getMessage());
                _uiState.postValue(UIState.ERROR);
            }
        });
    }
}