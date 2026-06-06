package com.infix.gamelatthe.ui.view.history_screen;

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
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHistory.setAdapter(adapter);
    }

    private void setupViewModel() {
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        PlayHistoryDao dao = MyDatabase.getInstance(requireContext()).playHistoryDao();
        viewModel.setDao(dao);
    }

    private void observeData() {
        viewModel._historyList.observe(getViewLifecycleOwner(), playHistories -> {
            // 5.1.9 ViewModel trả về danh sách top 10 cho View (Thông qua playHistories)
            if (playHistories != null) {
                // 5.1.10 View hiển thị danh sách top 10 lên màn hình
                adapter.updateList(playHistories);
            }
        });

        viewModel._uiState.observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SUCCESS:
                    binding.layoutLeaderboard.setVisibility(View.VISIBLE);
                    break;
                case EMPTY:
                    // 5.2.3 View nhận được thông báo và cập nhật giao diện (Báo chưa có dữ liệu)
                    binding.layoutLeaderboard.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Chưa có kỷ lục nào cho chế độ này!", Toast.LENGTH_SHORT).show();
                    break;
                case ERROR:
                    // 5.3.3 View nhận được thông báo và hiển thị Toast báo lỗi
                    binding.layoutLeaderboard.setVisibility(View.GONE);
                    break;
                case LOADING:
                    break;
            }
        });

        viewModel._errorMessage.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
    //  5.1.5 View hiển thị ô chọn cấp độ chơi (3 nút EASY, NORMAL, HARD đã có sẵn trên XML)
    private void setupClickListeners() {
        // 5.1.6 Người chơi chọn vào cấp độ muốn xem (Ví dụ: Chọn chế độ EASY)
        binding.btnEasy.setOnClickListener(v -> {
            // 5.1.7 View gửi sự kiện tới ViewModel yêu cầu lấy top 10 theo cấp độ đã chọn
            viewModel.getTop10(DifficultyEnum.EASY.name());
        });

        binding.btnNormal.setOnClickListener(v -> {
            viewModel.getTop10(DifficultyEnum.NORMAL.name());
        });

        binding.btnHard.setOnClickListener(v -> {
            viewModel.getTop10(DifficultyEnum.HARD.name());
        });
        // tương tự cho chế độ NORMAL, HARD
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}