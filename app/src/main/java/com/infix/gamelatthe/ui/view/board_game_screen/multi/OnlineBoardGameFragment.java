package com.infix.gamelatthe.ui.view.board_game_screen.multi;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentLobbyRoomBinding;
import com.infix.gamelatthe.databinding.FragmentOnlineBoardGameBinding;

public class OnlineBoardGameFragment extends Fragment {
    private FragmentOnlineBoardGameBinding binding;
    private static final String ARG_ROOM_ONLINE = "ARG_ROOM_ONLINE";

    private RoomOnline roomOnline;

    public static OnlineBoardGameFragment newInstance(RoomOnline roomOnline) {
        OnlineBoardGameFragment fragment = new OnlineBoardGameFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROOM_ONLINE, roomOnline);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                roomOnline = (RoomOnline) getArguments().getSerializable(ARG_ROOM_ONLINE);
            } catch (Exception e) {
                Log.e("OnlineBoardGameFragment", e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOnlineBoardGameBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }
}