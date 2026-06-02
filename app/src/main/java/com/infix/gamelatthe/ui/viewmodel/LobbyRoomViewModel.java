package com.infix.gamelatthe.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.infix.gamelatthe.common.RoomSnapshotCallback;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;

public class LobbyRoomViewModel extends ViewModel {
    private GameRepository repository = new GameRepository();

    private final MutableLiveData<Boolean> _isNetworkValid = new MutableLiveData<>();
    public LiveData<Boolean> isNetworkValid = _isNetworkValid;

    private final MutableLiveData<String> _notifyMsg = new MutableLiveData<>();
    public LiveData<String> notifyMsg = _notifyMsg;

    private final MutableLiveData<RoomOnline> _roomData = new MutableLiveData<>();
    public LiveData<RoomOnline> roomData = _roomData;

    public void startListeningToRoomByCode(String roomCode, RoomSnapshotCallback roomSnapshotCallback) {
        if(Boolean.FALSE.equals(isNetworkValid.getValue())) {
            _notifyMsg.setValue("Mạng không khả dụng");
            return;
        }
        repository.startListeningToRoomByCode(roomCode, roomSnapshotCallback);
    }

    public void setRoomOnlineState(RoomOnline roomOnline) {
        _roomData.setValue(roomOnline);
    }

    public void resetAllState() {
        _notifyMsg.setValue(null);
        _isNetworkValid.setValue(null);
        _roomData.setValue(null);
        repository.unregisterListeningToRoomByCode();
    }
}
