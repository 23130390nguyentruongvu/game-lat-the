package com.infix.gamelatthe.ui.view.board_game_screen.multi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.infix.gamelatthe.data.model.multi.CardOnline;
import com.infix.gamelatthe.data.model.multi.RoomOnline;
import com.infix.gamelatthe.databinding.FragmentOnlineBoardGameBinding;
import com.infix.gamelatthe.ui.view.MainActivity;
import com.infix.gamelatthe.ui.view.board_game_screen.BoardGameAdapter;
import com.infix.gamelatthe.ui.viewmodel.LobbyRoomViewModel;
import com.infix.gamelatthe.ui.viewmodel.OnlineBoardGameViewModel;

public class OnlineBoardGameFragment extends Fragment {
    private FragmentOnlineBoardGameBinding binding;
    private static final String ARG_ROOM_ONLINE = "ARG_ROOM_ONLINE";

    private RoomOnline roomOnline;
    private LobbyRoomViewModel lobbyRoomViewModel;
    private OnlineBoardGameViewModel onlineBoardGameViewModel;
    private BoardGameAdapter boardGameAdapter;
    private String currentUserId;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOnlineBoardGameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(MainActivity.FILE_INFO_USER, Context.MODE_PRIVATE);
        currentUserId = sharedPreferences.getString(MainActivity.KEY_UUID_USER, "");

        lobbyRoomViewModel = new ViewModelProvider(requireActivity()).get(LobbyRoomViewModel.class);
        onlineBoardGameViewModel = new ViewModelProvider(this).get(OnlineBoardGameViewModel.class);

        setupRecyclerView();
        observeRoomData();
    }

    private void setupRecyclerView() {
        boardGameAdapter = new BoardGameAdapter(card -> {
            if (card instanceof CardOnline && roomOnline != null) {
                if (roomOnline.getCurrentTurn() != null && !roomOnline.getCurrentTurn().equals(currentUserId)) {
                    // 7.3.3 Hiển thị Toast cảnh báo "Chưa tới lượt của bạn!".
                    Toast.makeText(requireContext(), "Chưa tới lượt của bạn!", Toast.LENGTH_SHORT).show();
                }
                onlineBoardGameViewModel.onCardClick((CardOnline) card, roomOnline, currentUserId);
            }
        });

        binding.rvBoardGame.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        binding.rvBoardGame.setAdapter(boardGameAdapter);

        if (roomOnline != null && roomOnline.getBoardGame() != null) {
            boardGameAdapter.updateCards(roomOnline.getBoardGame().getCards());
        }
    }

    private void observeRoomData() {
        lobbyRoomViewModel.roomData.observe(getViewLifecycleOwner(), room -> {
            if (room != null && room.getBoardGame() != null) {
                this.roomOnline = room;

                // 7.1.3 Trình lắng nghe nhận sự kiện, đồng bộ hoạt họa lật ngửa thẻ.
                boardGameAdapter.updateCards(room.getBoardGame().getCards());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}