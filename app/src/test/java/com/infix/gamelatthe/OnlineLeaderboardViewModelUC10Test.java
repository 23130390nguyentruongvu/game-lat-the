package com.infix.gamelatthe;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.data.repository.LeaderboardRepository;
import com.infix.gamelatthe.ui.viewmodel.LeaderboardViewModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class OnlineLeaderboardViewModelUC10Test {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LeaderboardViewModel viewModel;

    @Mock
    private LeaderboardRepository repository;

    @Mock
    private Observer<List<MatchHistoryItem>> historyObserver;

    @Mock
    private Observer<String> errorObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // LeaderboardViewModel đã được cập nhật constructor để hỗ trợ inject mock repository
        viewModel = new LeaderboardViewModel(repository);
        viewModel.getMatchHistory().observeForever(historyObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
    }

    @Test
    public void testUC10_KhiFirestoreTraVeDanhSachRong_PhaiCapNhatThongBaoLoi_10_3_1() {
        String userUUID = "test-uuid";

        // Bước 10.1.2: Gửi yêu cầu lấy lịch sử thi đấu
        viewModel.fetchUserHistory(userUUID);

        // Giả lập Repository nhận yêu cầu và bắt callback
        ArgumentCaptor<LeaderboardRepository.HistoryCallback> captor =
                ArgumentCaptor.forClass(LeaderboardRepository.HistoryCallback.class);
        verify(repository).getMatchesByUserUUID(eq(userUUID), captor.capture());

        // Bước 10.3.1: Giả lập Firestore trả về danh sách rỗng
        captor.getValue().onHistoryLoaded(new ArrayList<>());

        // Bước 10.3.2: Kiểm tra LiveData hiển thị thông báo không có dữ liệu
        Assert.assertEquals("Không có lịch sử thi đấu.", viewModel.getErrorMessage().getValue());
    }

    @Test
    public void testUC10_KhiCoDuLieu_PhaiDayVaoLiveDataDeHienThi_10_1_6() {
        String userUUID = "test-uuid";
        List<MatchHistoryItem> mockData = new ArrayList<>();
        
        // SỬA: Cập nhật constructor MatchHistoryItem khớp với 9 tham số (thêm userUUID ở đầu)
        mockData.add(new MatchHistoryItem(userUUID, "room1", "Easy", "HOST", "Opponent", "WIN", 10, 100, null));

        viewModel.fetchUserHistory(userUUID);

        ArgumentCaptor<LeaderboardRepository.HistoryCallback> captor =
                ArgumentCaptor.forClass(LeaderboardRepository.HistoryCallback.class);
        verify(repository).getMatchesByUserUUID(eq(userUUID), captor.capture());

        // Bước 10.1.3: Giả lập Firestore trả về dữ liệu lịch sử
        captor.getValue().onHistoryLoaded(mockData);

        // Bước 10.1.6: Kiểm tra LiveData nhận đúng số lượng phần tử để hiển thị lên View
        Assert.assertNotNull(viewModel.getMatchHistory().getValue());
        Assert.assertEquals(1, viewModel.getMatchHistory().getValue().size());
        Assert.assertEquals("WIN", viewModel.getMatchHistory().getValue().get(0).result);
    }
}
