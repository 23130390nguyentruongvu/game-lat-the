package com.infix.gamelatthe.ui.view.board_game_screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.databinding.FragmentBoardGameBinding;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;

public class BoardGameFragment extends Fragment {
    private FragmentBoardGameBinding binding;
    private BoardGameAdapter boardGameAdapter;
    private BoardGameViewModel boardGameViewModel;

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
}