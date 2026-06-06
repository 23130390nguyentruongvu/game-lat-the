package com.infix.gamelatthe.ui.view.home_screen;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.infix.gamelatthe.MyApplication;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.databinding.FragmentHomeBinding;
import com.infix.gamelatthe.ui.view.board_game_screen.BoardGameFragment;
import com.infix.gamelatthe.ui.view.history_screen.HistoryFragment;
import com.infix.gamelatthe.ui.view.home_screen.multi.OptionOnlineFragment;
import com.infix.gamelatthe.ui.view.leaderboard_screen.OnlineLeaderboardFragment;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;
import com.infix.gamelatthe.ui.viewmodel.HomeViewModel;

import java.util.List;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private Button btnStartGame;
    private Button btnHistory;

    private HomeViewModel homeViewModel;
    private BoardGameViewModel boardGameViewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }


    //6.1.0 Người chơi mở ứng dụng, hệ thống hiển thị màn hình chính.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initEvents();
        initViewModels();
        registerObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        unregisterObserver();
    }

    private void registerObserver() {
        try {
            MyApplication myApplication = (MyApplication) requireActivity().getApplication();
            myApplication.registerObserver(homeViewModel);
        } catch (Exception e) {
            Log.e("SVU", e.getMessage());
        }
    }

    private void unregisterObserver() {
        try {
            MyApplication myApplication = (MyApplication) requireActivity().getApplication();
            myApplication.removeObserver(homeViewModel);
        } catch (Exception e) {
            Log.e("SVU", e.getMessage());
        }
    }

    private void initEvents() {
        //-	1.1.2 Người chơi nhấn nút “Start game” trên View. View gửi sự kiện này đến ViewModel.
        binding.btnStartGame.setOnClickListener(v -> {
            Log.d("SVU", "1.1.2");
            showMessage("Đạng kiểm tra mạng");
                homeViewModel.onStartGameClicked();
        });

        binding.btnHistory.setOnClickListener(v -> {
            goToHistoryFragment();
        });

        //6.1.1 Người chơi nhấn chọn nút "Chơi trực tuyến".
        binding.btnStartGameOnline.setOnClickListener(v -> {
            //6.1.2 Hệ thống chuyển hướng sang giao diện sảnh trực tuyến
            goToOptionOnlineFragment();
        });

        // 10.1.0 Người chơi nhấn nút “Bảng Xếp Hạng” trên màn hình chính
        binding.btnLeaderboard.setOnClickListener(v -> {
            goToLeaderboardFragment();
        });
    }

    private void initViewModels() {
        //home viewmodel
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        //board game viewmodel
        boardGameViewModel = new ViewModelProvider(requireActivity()).get(BoardGameViewModel.class);

        //init observe
        //home viewmodel
        //-	1.4.2 View nhận trạng thái lỗi và hiển thị lỗi
        homeViewModel.errorState.observe(getViewLifecycleOwner(), this::showMessage);

        homeViewModel.isNetworkValid.observe(getViewLifecycleOwner(), isValid -> {
            if(isValid == null) return;
            String msg = !isValid?"Mạng không khả dụng":"Đã có mạng trở lại";
            showMessage(msg);
        });

        homeViewModel.levelList.observe(getViewLifecycleOwner(), levels -> {
            if (levels == null) return;
            //-	1.2.3 View nhận trạng thái và hiển thị thông báo "Không có cấp độ khả dụng"
            if(levels.isEmpty()) {
                showMessage("Không có cấp độ khả dụng");
                return;
            }
            //-	1.1.6 View nhận được thông báo tiến hành đưa các dữ liệu đó lên Dialog
            showSetupGameDialog(levels);
        });

        homeViewModel.boardGameState.observe(getViewLifecycleOwner(), boardGame -> {
            if(boardGame == null) return;

            boardGameViewModel.resetAllState();
            boardGameViewModel.setGameConfig(homeViewModel.gameConfigState.getValue());
            boardGameViewModel.setBoardGame(boardGame);
            //-	1.1.12 View nhận được thông báo về board game tiến hành gọi ra màn hình chuyên xử lí board game đó
            goToBoardGameFragment();
        });

        homeViewModel.setContext(requireContext().getApplicationContext());
    }

    private void goToBoardGameFragment() {
        FragmentTransaction transaction= requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main, new BoardGameFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToOptionOnlineFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcv_main, new OptionOnlineFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToLeaderboardFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fcv_main, new OnlineLeaderboardFragment())
                .addToBackStack(null)
                .commit();
    }

    private void goToHistoryFragment() {
        FragmentTransaction transaction= requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main, new HistoryFragment())
                .addToBackStack(null)
                .commit();
    }

    private void showMessage(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    private void showSetupGameDialog(List<String> levels) {
        if(levels.size() != 3) return;
        if(!levels.contains(DifficultyEnum.HARD.name())
                || !levels.contains(DifficultyEnum.EASY.name())
                || !levels.contains(DifficultyEnum.NORMAL.name()))
            return;

        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.fragment_setupgame);
        dialog.setCancelable(true);

        EditText edtPlayerName =
                dialog.findViewById(R.id.edtPlayerName);

        Button btnEasy =
                dialog.findViewById(R.id.btnEasy);
        Button btnNormal =
                dialog.findViewById(R.id.btnNormal);
        Button btnHard =
                dialog.findViewById(R.id.btnHard);
        Button btnStart =
                dialog.findViewById(R.id.btnStart);

        final DifficultyEnum[] selectedLevel =
                {DifficultyEnum.EASY};

        // DEFAULT SELECT
        btnEasy.setAlpha(1f);
        btnNormal.setAlpha(0.5f);
        btnHard.setAlpha(0.5f);

        // EASY
        btnEasy.setOnClickListener(v -> {
            selectedLevel[0] = DifficultyEnum.EASY;
            btnEasy.setAlpha(1f);
            btnNormal.setAlpha(0.5f);
            btnHard.setAlpha(0.5f);
        });

        // NORMAL
        btnNormal.setOnClickListener(v -> {
            selectedLevel[0] = DifficultyEnum.NORMAL;
            btnEasy.setAlpha(0.5f);
            btnNormal.setAlpha(1f);
            btnHard.setAlpha(0.5f);
        });

        // HARD
        btnHard.setOnClickListener(v -> {
            selectedLevel[0] = DifficultyEnum.HARD;
            btnEasy.setAlpha(0.5f);
            btnNormal.setAlpha(0.5f);
            btnHard.setAlpha(1f);
        });

        // START GAME
        //-	1.1.7 Người chơi nhập thông tin (Tên, cấp độ) và bấm xác nhận, View gửi dữ liệu cho ViewModel
        btnStart.setOnClickListener(v -> {
            String playerName =
                    edtPlayerName
                            .getText()
                            .toString()
                            .trim();

            // START GAME LOGIC
            homeViewModel.startGame(
                    playerName,
                    selectedLevel[0]
            );

            showMessage("Đang tiến hành tạo board");
            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}