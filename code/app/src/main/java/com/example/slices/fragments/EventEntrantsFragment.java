package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.databinding.EventEntrantsFragmentBinding;

/**
 * Fragment for displaying the list of entrants for a specific event
 */
public class EventEntrantsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";

    private EventEntrantsFragmentBinding binding;
    private int eventId;
    private String eventName;

    /**
     * Create a new instance of EventEntrantsFragment with event data
     * @param eventId ID of the event to display entrants for
     * @param eventName Name of the event for display
     * @return New fragment instance
     */
    public static EventEntrantsFragment newInstance(int eventId, String eventName) {
        EventEntrantsFragment fragment = new EventEntrantsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments passed through navigation
        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID, -1);
            eventName = getArguments().getString(ARG_EVENT_NAME, "Unknown Event");
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = EventEntrantsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set event name in header
        updateEventInfo();

        // Initialize the entrants list container
        setupEntrantsContainer();
    }

    /**
     * Update the event information display
     */
    private void updateEventInfo() {
        if (binding != null) {
            binding.tvEventName.setText(eventName);
            binding.tvEntrantCount.setText("Number of entrants");       // Placeholder for now
        }
    }

    /**
     * Set up the entrants list container
     */
    private void setupEntrantsContainer() {
        if (binding != null) {
            // Show the entrants list container and hide other states
            binding.entrantsListContainer.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}