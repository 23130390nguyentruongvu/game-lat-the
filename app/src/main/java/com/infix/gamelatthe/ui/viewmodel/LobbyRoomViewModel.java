package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.utils.Observer;

public class LobbyRoomViewModel extends ViewModel implements Observer {
    private GameRepository repository;

    private final MutableLiveData<Boolean> _isNetworkValid = new MutableLiveData<>();
    public LiveData<Boolean> isNetworkValid = _isNetworkValid;

    private final MutableLiveData<String> _notifyMsg = new MutableLiveData<>();
    public LiveData<String> notifyMsg = _notifyMsg;

    private final MutableLiveData<RoomOnline> _roomData = new MutableLiveData<>();
    public LiveData<RoomOnline> roomData = _roomData;

    public LobbyRoomViewModel() {
        this.repository = new GameRepository();
    }

    //Tạo để cho Test
    public LobbyRoomViewModel(GameRepository repository) {
        this.repository = repository;
    }

    public void startListeningToRoomByCode(String roomCode, RoomSnapshotCallback roomSnapshotCallback) {
        if(Boolean.FALSE.equals(isNetworkValid.getValue())) {
            _notifyMsg.setValue("Mạng không khả dụng");
            return;
        }
        repository.startListeningToRoomByCode(roomCode, roomSnapshotCallback);
    }

    public void leaveRoomOnline(String uuid, String roomCode, RoomOnlineListener roomOnlineListener) {
        if(Boolean.FALSE.equals(isNetworkValid.getValue())) {
            _notifyMsg.setValue("Mạng không khả dụng");
            return;
        }
        repository.leaveRoomOnline(uuid, roomCode, roomOnlineListener);
    }

    public void setRoomOnlineState(RoomOnline roomOnline) {
        _roomData.setValue(roomOnline);
    }

    public void startGameOnline(String roomCode, RoomOnlineListener roomOnlineListener) {
        if(Boolean.FALSE.equals(isNetworkValid.getValue())) {
            _notifyMsg.setValue("Mạng không khả dụng");
            return;
        }
        repository.startGameOnline(roomCode, roomOnlineListener);
    }

    public void setRepository(GameRepository repository) {
        this.repository = repository;
    }

    public void resetAllState() {
        _notifyMsg.setValue(null);
        _isNetworkValid.setValue(null);
        _roomData.setValue(null);
        repository.unregisterListeningToRoomByCode();
    }

    public void setIsNetworkValidState(boolean isNetworkValid) {
        _isNetworkValid.setValue(isNetworkValid);
    }

    @Override
    public void onUpdateNetworkValid(boolean isNetworkValid) {
        _isNetworkValid.postValue(isNetworkValid);
    }
}
