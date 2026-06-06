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
            // Lưu ý: LeaderboardViewModel cần được sửa để có thể inject repository vào (giống LobbyRoomViewModel)
            viewModel = new LeaderboardViewModel(repository);
            viewModel.getMatchHistory().observeForever(historyObserver);
            viewModel.getErrorMessage().observeForever(errorObserver);
        }

        @Test
        public void testUC10_KhiFirestoreTraVeDanhSachRong_PhaiCapNhatThongBaoLoi_10_3_1() {
            String userUUID = "test-uuid";

            // Gọi hàm fetch
            viewModel.fetchUserHistory(userUUID);

            // Giả lập Repository trả về danh sách rỗng (callback)
            ArgumentCaptor<LeaderboardRepository.HistoryCallback> captor =
                    ArgumentCaptor.forClass(LeaderboardRepository.HistoryCallback.class);
            verify(repository).getMatchesByUserUUID(eq(userUUID), captor.capture());

            captor.getValue().onHistoryLoaded(new ArrayList<>());

            // Kiểm tra LiveData báo lỗi (Bước 10.3.2)
            Assert.assertEquals("Không có lịch sử thi đấu.", viewModel.getErrorMessage().getValue());
        }

        @Test
        public void testUC10_KhiCoDuLieu_PhaiDayVaoLiveDataDeHienThi_10_1_6() {
            String userUUID = "test-uuid";
            List<MatchHistoryItem> mockData = new ArrayList<>();
            mockData.add(new MatchHistoryItem("room1", "Easy", "HOST", "Opponent", "WIN", 10, 100, null));

            viewModel.fetchUserHistory(userUUID);

            ArgumentCaptor<LeaderboardRepository.HistoryCallback> captor =
                    ArgumentCaptor.forClass(LeaderboardRepository.HistoryCallback.class);
            verify(repository).getMatchesByUserUUID(eq(userUUID), captor.capture());

            captor.getValue().onHistoryLoaded(mockData);

            // Kiểm tra LiveData nhận đúng số lượng phần tử
            Assert.assertEquals(1, viewModel.getMatchHistory().getValue().size());
            Assert.assertEquals("WIN", viewModel.getMatchHistory().getValue().get(0).result);
        }
    }

