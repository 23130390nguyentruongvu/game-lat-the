package com.infix.gamelatthe.ui.view.home_screen;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.databinding.FragmentHomeBinding;
import com.infix.gamelatthe.ui.view.board_game_screen.BoardGameFragment;
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initEvents();
        initViewModels();
    }

    private void initEvents() {
        //-	1.1.2 Người chơi nhấn nút “Start game” trên View. View gửi sự kiện này đến ViewModel.
        binding.btnStartGame.setOnClickListener(v -> {
                homeViewModel.onStartGameClicked();
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

        homeViewModel.levelList.observe(getViewLifecycleOwner(), levels -> {
            //-	1.2.3 View nhận trạng thái và hiển thị thông báo "Không có cấp độ khả dụng"
            if(levels == null || levels.isEmpty()) {
                showMessage("Không có cấp độ khả dụng");
                return;
            }
            //-	1.1.6 View nhận được thông báo tiến hành đưa các dữ liệu đó lên Dialog
            showSetupGameDialog(levels);
        });

        homeViewModel.boardGameState.observe(getViewLifecycleOwner(), boardGame -> {
            //-	1.1.12 View nhận được thông báo về board game tiến hành gọi ra màn hình chuyên xử lí board game đó
            boardGameViewModel.setGameConfig(homeViewModel.gameConfigState.getValue());
            boardGameViewModel.setBoardGame(boardGame);

            goToBoardGameFragment();
        });

        //
        homeViewModel.setContext(requireContext());
    }

    private void goToBoardGameFragment() {
        FragmentTransaction transaction= requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcv_main, new BoardGameFragment())
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
        dialog.setCancelable(false);

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