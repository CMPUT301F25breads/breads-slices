package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slices.adapters.OrganizerEventAdapter;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.OrganizerEventsFragmentBinding;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * OrganizerEventsFragment
 * Shows all events created by the current organizer, divided into
 * Upcoming, In Progress, and Past sections.
 *
 * Author: Juliana
 */
public class OrganizerEventsFragment extends Fragment {

    // View binding object automatically generated from your XML layout
    private OrganizerEventsFragmentBinding binding;

    // Lists for each section
    private final List<Event> upcomingEvents = new ArrayList<>();
    private final List<Event> inProgressEvents = new ArrayList<>();
    private final List<Event> pastEvents = new ArrayList<>();

    // Firebase / DB helper
    private final DBConnector db = new DBConnector();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout using view binding (organizer_events_fragment.xml)
        binding = OrganizerEventsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerViews with linear layout managers
        binding.rvUpcoming.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPast.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load organizer's events from the database
        loadOrganizerEvents();
    }

    private void loadOrganizerEvents() {
        //getAllFutureEvents is a placeholder for the actual organizer's events based on ID
        db.getAllFutureEvents(new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                // Clear old lists
                upcomingEvents.clear();
                inProgressEvents.clear();
                pastEvents.clear();

                // Get current time
                Calendar cal = Calendar.getInstance();
                long now = cal.getTimeInMillis();

                // Sort events
                for (Event e : events) {
                    long eventTime = e.getEventDate().toDate().getTime();
                    long regDeadline = e.getRegDeadline().toDate().getTime();

                    if (now < regDeadline) {
                        upcomingEvents.add(e);
                    } else if (now >= regDeadline && now < eventTime) {
                        inProgressEvents.add(e);
                    } else {
                        pastEvents.add(e);
                    }
                }

                // Use new adapter constructor with 'this' fragment
                binding.rvUpcoming.setAdapter(new OrganizerEventAdapter(requireContext(), upcomingEvents, OrganizerEventsFragment.this));
                binding.rvInProgress.setAdapter(new OrganizerEventAdapter(requireContext(), inProgressEvents, OrganizerEventsFragment.this));
                binding.rvPast.setAdapter(new OrganizerEventAdapter(requireContext(), pastEvents, OrganizerEventsFragment.this));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
