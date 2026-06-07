package com.infix.gamelatthe.ui.view.history_screen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.data.source.local.MyDatabase;
import com.infix.gamelatthe.data.source.local.PlayHistoryDao;
import com.infix.gamelatthe.databinding.FragmentHistoryBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupViewModel();
        observeData();
        setupClickListeners();

        // [10.1.1] Hệ thống lấy UUID đã lưu trong SharedPreferences (Đồng bộ với MainActivity)
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(MainActivity.FILE_INFO_USER, Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString(MainActivity.KEY_UUID_USER, "");

        // [10.1.2] Gửi yêu cầu lấy lịch sử thi đấu của người chơi theo UUID.
        if (!currentUserId.isEmpty()) {
            viewModel.loadMatchHistory(currentUserId);
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy mã định danh người chơi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHistory.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        PlayHistoryDao dao = MyDatabase.getInstance(requireContext()).playHistoryDao();
        viewModel.setDao(dao);
    }

    private void observeData() {
        viewModel.matchHistory.observe(getViewLifecycleOwner(), list -> {
            // [10.1.6] Hiển thị danh sách lịch sử thi đấu trực tuyến (UC10)
            if (list != null) {
                adapter.updateList(list);
            }
        });

        viewModel._uiState.observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            switch (state) {
                case SUCCESS:
                    binding.layoutLeaderboard.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    binding.layoutLeaderboard.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Chưa có kỷ lục nào!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    binding.layoutLeaderboard.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        });

        viewModel._errorMessage.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnEasy.setOnClickListener(v -> viewModel.getTop10(DifficultyEnum.EASY.name()));
        binding.btnNormal.setOnClickListener(v -> viewModel.getTop10(DifficultyEnum.NORMAL.name()));
        binding.btnHard.setOnClickListener(v -> viewModel.getTop10(DifficultyEnum.HARD.name()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}