package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.MainActivity;
import com.example.slices.SharedViewModel;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.models.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.databinding.MyEventsFragmentBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment showing the user's confirmed, waitlisted, and
 * past events
 * @author Brad Erdely
 */
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

        setupListeners();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Finds all events for a given entrant and sets the adapters accordingly
     */
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

    /**
     * Setup on item click listeners so clicking an events navigates to event details
     */
    public void setupListeners() {
        binding.confirmedList.setOnItemClickListener((parent, v, position, id)-> {
            sharedViewModel.setSelectedEvent(sharedViewModel.getEvents().get(position));
            ((MainActivity)requireActivity()).navigateToDetails();
        });

        binding.waitlistList.setOnItemClickListener((parent, v, position, id)-> {
            sharedViewModel.setSelectedEvent(sharedViewModel.getWaitlistedEvents().get(position));
            ((MainActivity)requireActivity()).navigateToDetails();
        });
    }

}
