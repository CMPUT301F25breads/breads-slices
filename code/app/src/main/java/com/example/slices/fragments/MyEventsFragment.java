package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.SharedViewModel;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.models.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.databinding.MyEventsFragmentBinding;

import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private MyEventsFragmentBinding binding;
    private SharedViewModel sharedViewModel;

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

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        setupEvents();

        //ArrayList<Event> events = new ArrayList<>();

        // Testing
        //for(int i = 0; i < 10; i++)
            //events.add(new Event("Testing"));

        //EventAdapter eventAdapter = new EventAdapter(requireContext(), events);
        //binding.confirmedList.setAdapter(eventAdapter);
        //binding.waitlistList.setAdapter(eventAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setupEvents() {
        DBConnector db = new DBConnector();
        db.getEventsForEntrant(sharedViewModel.getUser(), new EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                sharedViewModel.setEvents(events);
                sharedViewModel.setWaitlistedEvents(waitEvents);
                binding.confirmedList.setAdapter(new EventAdapter(requireContext(), events));
                binding.waitlistList.setAdapter(new EventAdapter(requireContext(), waitEvents));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

}
