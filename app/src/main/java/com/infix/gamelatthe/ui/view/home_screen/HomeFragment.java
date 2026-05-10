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
import androidx.lifecycle.ViewModelProvider;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.ui.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment {

    private Button btnStartGame;
    private Button btnHistory;

    private HomeViewModel homeViewModel;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );

        initViews(view);

        initViewModel();

        initEvents();

        return view;
    }

    private void initViews(View view) {

        btnStartGame = view.findViewById(R.id.btnStartGame);

        btnHistory = view.findViewById(R.id.btnHistory);
    }

    private void initViewModel() {

        homeViewModel = new ViewModelProvider(this)
                .get(HomeViewModel.class);
    }

    private void initEvents() {

        btnStartGame.setOnClickListener(v -> {
            showSetupGameDialog();
        });

        btnHistory.setOnClickListener(v -> {

            // TODO:
            // Open History Screen

        });
    }

    private void showSetupGameDialog() {

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
        btnStart.setOnClickListener(v -> {

            String playerName =
                    edtPlayerName
                            .getText()
                            .toString()
                            .trim();

            if (TextUtils.isEmpty(playerName)) {

                edtPlayerName.setError(
                        "Please enter player name"
                );

                return;
            }

            // START GAME LOGIC
            homeViewModel.startGame(
                    playerName,
                    selectedLevel[0]
            );

            dialog.dismiss();

            // TODO:
            // Open Board Game Screen
        });

        dialog.show();
    }
}