package com.infix.gamelatthe.ui.view.home_screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.infix.gamelatthe.R;
import com.infix.gamelatthe.databinding.FragmentHomeBinding;
import com.infix.gamelatthe.ui.viewmodel.BoardGameViewModel;
import com.infix.gamelatthe.ui.viewmodel.HomeViewModel;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private BoardGameViewModel boardGameViewModel;
    private HomeViewModel homeViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}