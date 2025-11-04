package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.models.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.databinding.MyEventsFragmentBinding;

import java.util.ArrayList;

public class MyEventsFragment extends Fragment {

    private MyEventsFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = MyEventsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<Event> events = new ArrayList<>();

        // Testing
        for(int i = 0; i < 10; i++)
            events.add(new Event("Testing"));

        EventAdapter eventAdapter = new EventAdapter(requireContext(), events);
        binding.myEventsList.setAdapter(eventAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
