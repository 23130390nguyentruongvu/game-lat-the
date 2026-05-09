package com.infix.gamelatthe.ui.view.history_screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.databinding.FragmentHistoryBinding;
import com.infix.gamelatthe.ui.viewmodel.HistoryViewModel;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private HistoryViewModel viewModel;
    private HistoryAdapter historyAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        com.infix.gamelatthe.data.source.local.MyDatabase db =
                androidx.room.Room.databaseBuilder(requireContext(),
                        com.infix.gamelatthe.data.source.local.MyDatabase.class, "game_database").build();

        com.infix.gamelatthe.data.repository.HistoryRepository repo =
                new com.infix.gamelatthe.data.repository.HistoryRepository(db.playHistoryDao());

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        viewModel.setRepository(repo);

        initRecyclerView();
    }

    private void initRecyclerView() {
    }
}