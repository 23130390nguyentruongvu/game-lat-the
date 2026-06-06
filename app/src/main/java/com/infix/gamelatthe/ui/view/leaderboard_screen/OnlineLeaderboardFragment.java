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
        return inflater.inflate(R.layout.fragment_online_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_view_leaderboard);
        emptyStateView = view.findViewById(R.id.empty_state_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);

        setupRecyclerView();
        observeHistory();
        loadUserHistory();

        // 10.1.8 Người chơi nhấn "Quay lại" để trở về màn hình trước.
        // 10.3.3 Người chơi có thể quay lại màn hình trước.
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
        // 10.1.1 Hệ thống lấy UUID đã lưu trong SharedPreferences.
        SharedPreferences prefs = requireActivity().getSharedPreferences("player_prefs", Context.MODE_PRIVATE);
        String userUUID = prefs.getString("user_uuid", null);

        // 10.2.1 Hệ thống không tìm thấy UUID trong SharedPreferences.
        if (userUUID == null) {
            // 10.2.2 Hệ thống tự động tạo UUID mới.
            userUUID = UUID.randomUUID().toString();
            // 10.2.3 Hệ thống lưu UUID mới vào SharedPreferences.
            prefs.edit().putString("user_uuid", userUUID).apply();
            // 10.2.4 Hệ thống hiển thị thông báo: "Bạn chưa có lịch sử thi đấu trực tuyến."
            showEmptyState("Bạn chưa có lịch sử thi đấu trực tuyến.");
            return;
        }

        // 10.1.2 Hệ thống gửi yêu cầu lấy lịch sử thi đấu của người chơi theo UUID.
        viewModel.fetchUserHistory(userUUID);
    }

    private void observeHistory() {
        viewModel.getMatchHistory().observe(getViewLifecycleOwner(), this::showHistoryList);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showEmptyState);
    }

    private void showHistoryList(List<MatchHistoryItem> historyItems) {
        // 10.1.6 Hệ thống hiển thị danh sách lịch sử thi đấu trực tuyến của người chơi.
        if (historyItems != null && !historyItems.isEmpty()) {
            adapter.submitList(historyItems);
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
            // 10.3.1 FireStore trả về danh sách rỗng.
            // 10.3.2 Hệ thống hiển thị trạng thái không có dữ liệu.
            showEmptyState("Không có lịch sử thi đấu nào.");
        }
    }

    private void showEmptyState(String message) {
        recyclerView.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        if (emptyStateTextView != null) {
            emptyStateTextView.setText(message);
        }
    }
}
