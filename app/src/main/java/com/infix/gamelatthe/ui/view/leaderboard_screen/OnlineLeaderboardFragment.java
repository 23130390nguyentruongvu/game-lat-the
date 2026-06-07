package com.infix.gamelatthe.ui.view.leaderboard_screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.data.model.multi.MatchHistoryItem;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.viewmodel.LeaderboardViewModel;

import java.util.List;
import java.util.UUID;

public class OnlineLeaderboardFragment extends Fragment {

    private LeaderboardViewModel viewModel;
    private MatchHistoryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;
    private TextView emptyStateTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Nạp giao diện XML cho fragment
        return inflater.inflate(R.layout.fragment_online_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các View từ XML
        recyclerView = view.findViewById(R.id.recycler_view_leaderboard);
        emptyStateView = view.findViewById(R.id.empty_state_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        setupRecyclerView();
        observeHistory();
        loadUserHistory();

        // [10.1.8] Người chơi nhấn "Quay lại" để trở về màn hình trước
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        }
    }

    private void setupRecyclerView() {
        adapter = new MatchHistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadUserHistory() {
        // [10.1.1] Hệ thống lấy UUID từ SharedPreferences (Đồng bộ với MainActivity)
        SharedPreferences sharedPreferences = requireActivity()
                .getSharedPreferences(MainActivity.FILE_INFO_USER, Context.MODE_PRIVATE);
        String userUUID = sharedPreferences.getString(MainActivity.KEY_UUID_USER, null);

        // [10.2] Alternate Flow - Trường hợp chưa có UUID (máy mới cài app)
        if (userUUID == null) {
            // [10.2.2] Tự động tạo UUID mới
            userUUID = UUID.randomUUID().toString();
            // [10.2.3] Lưu UUID mới vào bộ nhớ
            sharedPreferences.edit().putString(MainActivity.KEY_UUID_USER, userUUID).apply();
            // [10.2.4] Hiển thị thông báo rỗng vì người dùng mới chưa đấu trận nào
            showEmptyState("Bạn chưa có lịch sử thi đấu trực tuyến.");
            return;
        }

        // [10.1.2] Có UUID -> Gửi yêu cầu lên ViewModel để lấy lịch sử từ Firebase
        viewModel.fetchUserHistory(userUUID);
    }

    private void observeHistory() {
        // Theo dõi dữ liệu lịch sử từ ViewModel
        viewModel.getMatchHistory().observe(getViewLifecycleOwner(), this::showHistoryList);

        // Theo dõi thông báo lỗi hoặc trạng thái trống từ ViewModel
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showEmptyState);
    }

    private void showHistoryList(List<MatchHistoryItem> historyItems) {
        // [10.1.6] Hệ thống hiển thị danh sách lịch sử thi đấu
        if (historyItems != null && !historyItems.isEmpty()) {
            adapter.submitList(historyItems);
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            // [10.3.2] Hiển thị trạng thái không có dữ liệu
            showEmptyState("Không có lịch sử thi đấu nào.");
        }
    }

    private void showEmptyState(String message) {
        // Ẩn danh sách và hiện thông báo thông tin trống
        recyclerView.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        if (emptyStateTextView != null) {
            emptyStateTextView.setText(message);
        }
    }
}