package com.infix.gamelatthe.ui.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.model.BoardGame;
import com.infix.gamelatthe.data.model.Card;
import com.infix.gamelatthe.data.model.GameConfig;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.data.source.remote.RemoteDataSource;
import com.infix.gamelatthe.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private GameRepository repository = new GameRepository();
    private GameConfig config;
    private GameConfig currentConfig;

    private final MutableLiveData<List<String>> _levelList = new MutableLiveData<>();
    public LiveData<List<String>> levelList = _levelList;

    private final MutableLiveData<GameConfig> _gameConfigState = new MutableLiveData<>();
    public LiveData<GameConfig> gameConfigState = _gameConfigState;

    private final MutableLiveData<String> _errorState = new MutableLiveData<>();
    public LiveData<String> errorState = _errorState;

    private final MutableLiveData<BoardGame> _boardGameState = new MutableLiveData<>();
    public LiveData<BoardGame> boardGameState = _boardGameState;

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public void onStartGameClicked() {
        //-	1.1.3 ViewModel gọi NetworkUtils để kiểm tra trạng thái kết nối mạng của thiết bị
        if (!NetworkUtils.isNetworkAvailable(context)) {
            //(Rẽ nhánh từ 1.1.3) Nếu thiết bị người dùng không có kết nối mạng
            //-	1.4.1 ViewModel cập nhật trạng thái lỗi không có mạng
            _errorState.setValue("No Internet Connection");
            //-	1.4.3 ViewModel dừng các logic bên dưới và quay lại bước 1.1.1
            return;
        }

        //-	1.1.4 ViewModel gọi đến Repository/Source để lấy thông tin về các cấp độ chơi
        Log.d("SVU", "1.1.4");
        repository.getLevels(new RemoteDataSource.LevelsCallback() {
            @Override
            public void onSuccess(List<String> levels) {
                //    (Rẽ nhánh từ 1.1.5) Không có dữ liệu cấp độ
                if (levels == null || levels.isEmpty()) {
                    _errorState.setValue("No levels available");
                    //-	1.2.1 ViewModel nhận danh sách cấp độ rỗng
                    //-	1.2.2 ViewModel cập nhật trạng thái dữ liệu rỗng
                    _levelList.setValue(new ArrayList<>());
                    //-	1.2.4 Quay lại bước 1.1.1

                } else {
                    //-	1.1.5 ViewModel nhận được dữ liệu và danh sách các cấp độ cho View xử lí
                    _levelList.setValue(levels);
                }
            }

            @Override
            public void onError(String error) {
                _errorState.setValue(error);
            }
        });
    }

    public void startGame(String playerName,
                          DifficultyEnum difficulty) {
        //-	1.1.8 Nếu tên người chơi là null hoặc rỗng
        //o	thiết lập tên mặc định người chơi là “User1”
        if(playerName== null||playerName.isEmpty()) {
            playerName = "User1";
        }

        //-	1.1.9 ViewModel tiến hành lưu lại các thông tin cấu hình của người chơi vào một đối tượng
        GameConfig config =
                new GameConfig(
                        playerName,
                        difficulty
                );

        //-	1.1.10 ViewModel lấy ra cấp độ đã chọn để dùng Repository/Source lấy ra board game tương ứng
        repository.getBoard(difficulty, new RemoteDataSource.BoardCallback() {
            @Override
            public void onSuccess(List<Card> cards) {
                //-	1.1.11 ViewModel nhận được dữ liệu board game, gửi thông báo đến View
                BoardGame boardGame = new BoardGame(
                        cards,
                        System.currentTimeMillis()
                );
                _boardGameState.setValue(boardGame);
            }

            @Override
            public void onError(String error) {

            }
        });

        // VALIDATION
        if (!config.isValid()) {

            _errorState.setValue(
                    "Invalid game config"
            );

            return;
        }

        currentConfig = config;

        _gameConfigState.setValue(config);
    }

    public void setBoardGameState(BoardGame boardGameState) {
        _boardGameState.setValue(boardGameState);
    }

    public void setLevelList(List<String> levelList) {
        _levelList.setValue(levelList);
    }

    public void setConfigState(GameConfig gameConfig) {
        _gameConfigState.setValue(gameConfig);
    }
}