package com.infix.gamelatthe.ui.view.home_screen.multi;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.infix.gamelatthe.MyApplication;
import com.infix.gamelatthe.R;
import com.infix.gamelatthe.common.DifficultyEnum;
import com.infix.gamelatthe.common.RoomOnlineListener;
import com.infix.gamelatthe.common.UserRole;
import com.infix.gamelatthe.data.model.multi.PlayerOnline;
import com.infix.gamelatthe.databinding.FragmentOptionOnlineBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.viewmodel.HomeViewModel;

public class OptionOnlineFragment extends Fragment {
    private FragmentOptionOnlineBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOptionOnlineBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEvent();
        registerObserver();
        initHomeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        homeViewModel.resetAllState();
        unregisterObserver();
    }

    private void initHomeViewModel() {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
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

    private void setEvent() {
        //create room
        binding.btnCreateRoomOptionOnline.setOnClickListener(v -> {
            //6.1.3 Người chơi nhấn nút "Tạo phòng mới" (Vai trò HOST). Hệ thống hiển thị popup cấu hình phòng đấu.
            showDialogConfigRoomOnline();
        });
    }

    private void showMessage(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void showDialogConfigRoomOnline() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.fragment_setupgame);
        dialog.setCancelable(true);

        EditText edtPlayerName = dialog.findViewById(R.id.edtPlayerName);

        Button btnEasy = dialog.findViewById(R.id.btnEasy);
        Button btnMedium = dialog.findViewById(R.id.btnNormal);
        Button btnHard = dialog.findViewById(R.id.btnHard);
        Button btnCreateRoom = dialog.findViewById(R.id.btnStart);

        edtPlayerName.setHint("Nhập tên hiển thị");

        btnCreateRoom.setText("XÁC NHẬN TẠO PHÒNG");

        final String[] selectedDifficulty = {"EASY"};

        btnEasy.setAlpha(1.0f);
        btnMedium.setAlpha(0.5f);
        btnHard.setAlpha(0.5f);

        btnEasy.setOnClickListener(v -> {
            selectedDifficulty[0] = DifficultyEnum.EASY.name();
            btnEasy.setAlpha(1.0f);
            btnMedium.setAlpha(0.5f);
            btnHard.setAlpha(0.5f);
        });

        btnMedium.setOnClickListener(v -> {
            selectedDifficulty[0] = DifficultyEnum.NORMAL.name();
            btnEasy.setAlpha(0.5f);
            btnMedium.setAlpha(1.0f);
            btnHard.setAlpha(0.5f);
        });

        btnHard.setOnClickListener(v -> {
            selectedDifficulty[0] = DifficultyEnum.HARD.name();
            btnEasy.setAlpha(0.5f);
            btnMedium.setAlpha(0.5f);
            btnHard.setAlpha(1.0f);
        });

        //6.1.4 Người chơi chọn độ khó ván game (Easy / Medium / Hard), nhập tên và nhấn nút "Xác nhận tạo phòng".
        btnCreateRoom.setOnClickListener(v -> {
            String displayName = edtPlayerName.getText().toString();
            if (displayName.isEmpty()) {
                showMessage("Tên hiển thị không được để trống");
                return;
            }
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(
                    MainActivity.FILE_INFO_USER,
                    Context.MODE_PRIVATE
            );
            String uuid = sharedPreferences.getString(MainActivity.KEY_UUID_USER, null);
            if (uuid == null) {
                showMessage("Uuid chưa tồn tại");
                return;
            }

            PlayerOnline playerOnline = new PlayerOnline(
                    uuid,
                    displayName,
                    0,
                    true,
                    UserRole.HOST.role
            );

            showMessage("Đang tiến hành tạo phòng");
            homeViewModel.createRoomOnline(
                    playerOnline,
                    selectedDifficulty[0],
                    new RoomOnlineListener() {
                        @Override
                        public void onSuccess() {
                            //6.1.6 Hệ thống hiển thị giao diện Phòng chờ, công khai mã
                            // phòng ra màn hình và hiển thị trạng thái "Đang chờ đối thủ tham gia...".
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fcv_main, new LobbyRoomFragment())
                                    .addToBackStack(null)
                                    .commit();
                        }

                        @Override
                        public void onFailure() {

                        }
                    }
            );

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