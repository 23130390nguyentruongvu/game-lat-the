package com.infix.gamelatthe.ui.view.home_screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.infix.gamelatthe.R;

public class HomeFragment extends Fragment {

    private Button btnStartGame;
    private Button btnHistory;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnStartGame = view.findViewById(R.id.btnStartGame);
        btnHistory = view.findViewById(R.id.btnHistory);

        // UC1.1: chỉ start game (chưa config level ở UI này)
        btnStartGame.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Start Game clicked", Toast.LENGTH_SHORT).show();

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .commit();
        });

        // UC1.2: view history
        btnHistory.setOnClickListener(v -> {
            Toast.makeText(getContext(), "History clicked", Toast.LENGTH_SHORT).show();

            // TODO navigate history screen
        });

        return view;
    }
}