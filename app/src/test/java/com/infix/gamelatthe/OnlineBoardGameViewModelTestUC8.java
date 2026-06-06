package com.infix.gamelatthe;

import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.data.repository.GameRepository;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class OnlineBoardGameViewModelTestUC8 {

    // Bắt buộc phải có để test các biến LiveData chạy đồng bộ
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private OnlineBoardGameViewModel viewModel;
    private GameRepository mockGameRepository;
    private MockedConstruction<GameRepository> mockedConstruction;

    @Mock
    private Observer<String> gameOverObserver;

    @Mock
    private Observer<Boolean> networkErrorObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // 1. CHẶN FIREBASE: Không cho Firebase khởi tạo để tránh Crash (Mock Repository)
        mockedConstruction = mockConstruction(GameRepository.class, (mock, context) -> {});

        // 2. Khởi tạo ViewModel (Nó sẽ tự động lấy cái Repository ảo vừa tạo ở trên)
        viewModel = new OnlineBoardGameViewModel();
        mockGameRepository = mockedConstruction.constructed().get(0);

        // 3. Gắn trình lắng nghe vào các biến LiveData Public
        viewModel.gameOverEvent.observeForever(gameOverObserver);
        viewModel.networkError.observeForever(networkErrorObserver);
    }

    @After
    public void tearDown() {
        // Đóng bộ chặn sau mỗi lần Test
        if (mockedConstruction != null) {
            mockedConstruction.close();
        }
    }

    // ==========================================
    // TEST CASE 1: BỎ CUỘC (LUỒNG ALTERNATE FLOW 8.2)
    // ==========================================
    @Test
    public void testAbandonGame_KhiUserBamBoCuoc_ThiDoiThuSeThang() {
        // 1. Chuẩn bị Mock Data
        RoomOnline room = new RoomOnline();
        room.setRoomId("room_123");

        // Set List Player để hàm abandonGame không bị return ngang
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("user_me", "Me", 2, true, "HOST"));
        players.add(new PlayerOnline("user_enemy", "Enemy", 1, true, "GUEST"));
        room.setPlayers(players);

        // 2. Kích hoạt luồng bấm bỏ cuộc
        viewModel.abandonGame("user_me", room);

        ArgumentCaptor<RoomOnlineListener> captor = ArgumentCaptor.forClass(RoomOnlineListener.class);

        // 3. Khẳng định ViewModel có gửi dữ liệu đối thủ (user_enemy) lên server không
        verify(mockGameRepository).endRoomOnline(
                eq("room_123"),
                eq("ABANDONED"),
                eq("user_enemy"), // Kiểm tra đối thủ được xét thắng
                captor.capture()
        );

        // 4. Giả lập Server Firebase phản hồi thành công
        captor.getValue().onSuccess("OK");

        // 5. Khẳng định UI nhận được lệnh ngừng lắng nghe và hiển thị đối thủ thắng
        verify(mockGameRepository).stopListeningToRoom();
        verify(gameOverObserver).onChanged("user_enemy");
    }

    // ==========================================
    // TEST CASE 2: LỖI MẠNG (LUỒNG EXCEPTION 8.3)
    // ==========================================
    @Test
    public void testExecuteEndGame_KhiFirebaseLoiMatMang_ThiHienDialogBaoLoi() {
        // 1. Chuẩn bị Mock Data
        RoomOnline room = new RoomOnline();
        room.setRoomId("room_123");

        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("user_me", "Me", 2, true, "HOST"));
        players.add(new PlayerOnline("user_enemy", "Enemy", 1, true, "GUEST"));
        room.setPlayers(players);

        // 2. Kích hoạt luồng game
        viewModel.abandonGame("user_me", room);

        ArgumentCaptor<RoomOnlineListener> captor = ArgumentCaptor.forClass(RoomOnlineListener.class);
        verify(mockGameRepository).endRoomOnline(anyString(), anyString(), anyString(), captor.capture());

        // 3. KỊCH BẢN NGOẠI LỆ: Giả lập rớt mạng (gọi onFailure)
        captor.getValue().onFailure();

        // 4. Khẳng định LiveData bắn tín hiệu 'true' để UI hiện thông báo lỗi
        verify(networkErrorObserver).onChanged(true);
    }
}