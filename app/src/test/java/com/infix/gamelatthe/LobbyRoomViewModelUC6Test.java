package com.infix.gamelatthe;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.ui.viewmodel.LobbyRoomViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LobbyRoomViewModelUC6Test {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LobbyRoomViewModel viewModel;

    @Mock
    private GameRepository repository;

    @Mock
    private Observer<String> notifyMsgObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new LobbyRoomViewModel(repository);
        viewModel.notifyMsg.observeForever(notifyMsgObserver);
    }

    @Test
    public void startGameOnline_KhiMatMang_SeKhongGoiRepository_VaHienThongBaoLoi() {
        //Thiết lập trạng thái mất mạng
        viewModel.setIsNetworkValidState(false);

        String roomCode = "ROOM123";
        RoomOnlineListener mockListener = org.mockito.Mockito.mock(RoomOnlineListener.class);
        viewModel.startGameOnline(roomCode, mockListener);

        verify(repository, never()).startGameOnline(eq(roomCode), any());
        assertEquals("Mạng không khả dụng", viewModel.notifyMsg.getValue());
        verify(notifyMsgObserver).onChanged("Mạng không khả dụng");
    }

    @Test
    public void startGameOnline_KhiCoMangHopLe_SeChuyenTiepLenhXuongRepository() {
        //Thiết lập trạng thái có mạng
        viewModel.setIsNetworkValidState(true);

        String roomCode = "ROOM123";
        RoomOnlineListener mockListener = org.mockito.Mockito.mock(RoomOnlineListener.class);
        viewModel.startGameOnline(roomCode, mockListener);

        verify(repository).startGameOnline(eq(roomCode), eq(mockListener));
    }

    @Test
    public void leaveRoomOnline_KhiMatMang_SeChanLaiNgayTaiViewModel() {
        viewModel.setIsNetworkValidState(false);

        String uuid = "USER_V_456";
        String roomCode = "ROOM123";
        RoomOnlineListener mockListener = org.mockito.Mockito.mock(RoomOnlineListener.class);
        viewModel.leaveRoomOnline(uuid, roomCode, mockListener);

        assertEquals("Mạng không khả dụng", viewModel.notifyMsg.getValue());

        verify(repository, never()).leaveRoomOnline(eq(uuid), eq(roomCode), any());
    }

    @Test
    public void leaveRoomOnline_KhiCoMangHopLe_SeGoiRepositoryDeXuLyXoaPhong() {
        viewModel.setIsNetworkValidState(true);

        String uuid = "USER_V_456";
        String roomCode = "ROOM123";
        RoomOnlineListener mockListener = org.mockito.Mockito.mock(RoomOnlineListener.class);
        viewModel.leaveRoomOnline(uuid, roomCode, mockListener);

        verify(repository).leaveRoomOnline(eq(uuid), eq(roomCode), eq(mockListener));
    }

    @Test
    public void testUC6_KhiFirestoreDongBoRoomStatusLaPLAYING_DuLieuLiveDataPhaiCapNhat() {
        RoomOnline mockRoom = new RoomOnline();
        mockRoom.setStatus("PLAYING");

        Observer<RoomOnline> roomObserver = org.mockito.Mockito.mock(Observer.class);
        viewModel.roomData.observeForever(roomObserver);

        viewModel.setRoomOnlineState(mockRoom);

        assertEquals("PLAYING", viewModel.roomData.getValue().getStatus());
        verify(roomObserver).onChanged(mockRoom);
    }

    @Test
    public void testUC6_KhiFirestoreDongBoRoomStatusLaWAITING_DuLieuPhaiGiuNguyen_ChuaDuocChuyenManHinh() {
        RoomOnline mockRoom = new RoomOnline();
        mockRoom.setStatus("WAITING");

        Observer<RoomOnline> roomObserver = org.mockito.Mockito.mock(Observer.class);
        viewModel.roomData.observeForever(roomObserver);

        viewModel.setRoomOnlineState(mockRoom);

        assertEquals("WAITING", viewModel.roomData.getValue().getStatus());
    }
}