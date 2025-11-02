package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.databinding.MyEventsFragmentBinding;
import com.example.slices.databinding.MyEventsOrgFragmentBinding;

import java.util.ArrayList;

public class MyEventsOrgFragment extends Fragment {
    private MyEventsOrgFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = MyEventsOrgFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
