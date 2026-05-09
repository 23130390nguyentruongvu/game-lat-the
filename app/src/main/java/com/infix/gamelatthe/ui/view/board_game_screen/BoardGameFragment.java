package com.infix.gamelatthe.ui.view.board_game_screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;

public class BoardGameFragment extends Fragment {

    private BoardGameViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board_game, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        observeErrorEvent();
    }

    private void observeErrorEvent() {
        if (viewModel != null) {
            viewModel.errorEvent.observe(getViewLifecycleOwner(), errorMessage -> {
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    // Bước 4.2.3 & 4.3.3: View nhận thông báo từ LiveData và hiển thị Toast
                    showToast(errorMessage);
                }
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}