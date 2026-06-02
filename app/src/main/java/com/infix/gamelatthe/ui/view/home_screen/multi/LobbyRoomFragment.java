package com.infix.gamelatthe.ui.view.home_screen.multi;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.databinding.FragmentLobbyRoomBinding;

public class LobbyRoomFragment extends Fragment {
    private FragmentLobbyRoomBinding binding;
    private static final String ARG_USER_ROLE = "ARG_USER_ROLE";
    private static final String ARG_ROOM_CODE = "ARG_ROOM_CODE";

    private String userRole;
    private String roomCode;

    public static LobbyRoomFragment newInstance(String userRole, String roomCode) {
        LobbyRoomFragment fragment = new LobbyRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ROLE, userRole);
        args.putString(ARG_ROOM_CODE, roomCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userRole = getArguments().getString(ARG_USER_ROLE);
            roomCode = getArguments().getString(ARG_ROOM_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLobbyRoomBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}