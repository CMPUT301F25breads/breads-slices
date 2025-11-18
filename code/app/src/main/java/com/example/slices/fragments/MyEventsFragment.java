package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slices.SharedViewModel;

import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.EntrantEventCallback;
import com.example.slices.models.Event;
import com.example.slices.adapters.EventAdapter;
import com.example.slices.databinding.MyEventsFragmentBinding;

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

        EventController.getEventsForEntrant(sharedViewModel.getUser(), new EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                sharedViewModel.setEvents(events);
                sharedViewModel.setWaitlistedEvents(waitEvents);

                EventAdapter confirmedAdapter = new EventAdapter(requireContext(), events, MyEventsFragment.this);
                binding.confirmedList.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.confirmedList.setAdapter(confirmedAdapter);

                EventAdapter waitlistAdapter = new EventAdapter(requireContext(), waitEvents, MyEventsFragment.this);
                binding.waitlistList.setLayoutManager(new LinearLayoutManager(requireContext()));
                binding.waitlistList.setAdapter(waitlistAdapter);

            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }


}
