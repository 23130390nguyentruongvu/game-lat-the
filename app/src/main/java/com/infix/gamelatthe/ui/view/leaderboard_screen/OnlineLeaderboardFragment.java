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
        // SỬA TẠI ĐÂY: Dùng đúng file và key từ MainActivity để lấy ID đã chơi game
        SharedPreferences prefs = requireActivity().getSharedPreferences(MainActivity.FILE_INFO_USER, Context.MODE_PRIVATE);
        String userUUID = prefs.getString(MainActivity.KEY_UUID_USER, null);

        if (userUUID == null) {
            showEmptyState("Bạn chưa có lịch sử thi đấu trực tuyến.");
            return;
        }

        viewModel.fetchUserHistory(userUUID);
    }

    private void observeHistory() {
        viewModel.getMatchHistory().observe(getViewLifecycleOwner(), this::showHistoryList);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showEmptyState);
    }

    private void showHistoryList(List<MatchHistoryItem> historyItems) {
        if (historyItems != null && !historyItems.isEmpty()) {
            adapter.submitList(historyItems);
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        } else {
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
