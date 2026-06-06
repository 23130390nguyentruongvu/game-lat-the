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

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private OnlineBoardGameViewModel viewModel;
    private GameRepository mockGameRepository;

    // Đối tượng cực kỳ quan trọng để đánh chặn Firebase
    private MockedConstruction<GameRepository> mockedConstruction;

    @Mock
    private Observer<String> gameOverObserver;

    @Mock
    private Observer<Boolean> networkErrorObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // 1. KÍCH HOẠT ĐÁNH CHẶN: Ép toàn bộ lệnh "new GameRepository()" biến thành đồ giả
        // Nhờ vậy RemoteDataSource và Firebase sẽ KHÔNG bao giờ được gọi
        mockedConstruction = mockConstruction(GameRepository.class, (mock, context) -> {
            // Không làm gì cả
        });

        // 2. Khởi tạo ViewModel an toàn (sẽ không bị Crash báo đỏ nữa)
        viewModel = new OnlineBoardGameViewModel();

        // 3. Lấy đối tượng giả lập (Mock) đã sinh ra để dùng cho các hàm Test bên dưới
        mockGameRepository = mockedConstruction.constructed().get(0);

        // 4. Gắn các trình lắng nghe LiveData
        viewModel.gameOverEvent.observeForever(gameOverObserver);
        viewModel.networkError.observeForever(networkErrorObserver);
    }

    @After
    public void tearDown() {
        // BẮT BUỘC: Đóng bộ đánh chặn sau khi chạy xong mỗi test
        if (mockedConstruction != null) {
            mockedConstruction.close();
        }
    }

    // ==========================================
    // KIỂM THỬ HÀM: abandonGame() - Bỏ cuộc (Giữ nguyên cấu trúc List)
    // ==========================================
    @Test
    public void testAbandonGame_KhiUserBamBoCuoc_ThiDoiThuSeThang() {
        RoomOnline room = new RoomOnline();
        room.setRoomId("room_xyz");

        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("current_user", "Me", 2, true, "HOST"));
        players.add(new PlayerOnline("opponent_user", "Opponent", 1, true, "GUEST"));
        room.setPlayers(players);

        // Thực thi: User bấm bỏ cuộc
        viewModel.abandonGame("current_user", room);

        ArgumentCaptor<RoomOnlineListener> captor = ArgumentCaptor.forClass(RoomOnlineListener.class);

        // Xác minh gọi hàm endRoomOnline chính xác
        verify(mockGameRepository).endRoomOnline(
                eq("room_xyz"),
                eq("ABANDONED"),
                eq("opponent_user"),
                captor.capture()
        );

        // Giả lập Firebase trả về onSuccess
        captor.getValue().onSuccess("Trận đấu kết thúc!");

        verify(mockGameRepository).stopListeningToRoom();
        verify(gameOverObserver).onChanged("opponent_user");
    }

    // ==========================================
    // KIỂM THỬ HÀM: Luồng Ngoại Lệ (Lỗi rớt mạng)
    // ==========================================
    @Test
    public void testExecuteEndGame_KhiFirebaseLoiMatMang_ThiHienDialogBaoLoi() {
        RoomOnline room = new RoomOnline();
        room.setRoomId("room_xyz");

        // ---- ĐOẠN BỔ SUNG: Khởi tạo danh sách người chơi để không bị return ngang ----
        List<PlayerOnline> players = new ArrayList<>();
        players.add(new PlayerOnline("current_user", "Me", 2, true, "HOST"));
        players.add(new PlayerOnline("opponent_user", "Opponent", 1, true, "GUEST"));
        room.setPlayers(players);
        // -----------------------------------------------------------------------------

        // Kích hoạt luồng bỏ cuộc
        viewModel.abandonGame("current_user", room);

        ArgumentCaptor<RoomOnlineListener> captor = ArgumentCaptor.forClass(RoomOnlineListener.class);
        verify(mockGameRepository).endRoomOnline(any(), any(), any(), captor.capture());

        // KỊCH BẢN NGOẠI LỆ: Giả lập Firebase báo thất bại do rớt mạng
        captor.getValue().onFailure();

        // Khẳng định LiveData báo lỗi mạng phải kích hoạt
        verify(networkErrorObserver).onChanged(true);
    }
}
