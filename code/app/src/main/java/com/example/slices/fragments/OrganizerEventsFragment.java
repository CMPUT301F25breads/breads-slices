package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slices.SharedViewModel;
import com.example.slices.adapters.OrganizerEventAdapter;

import com.example.slices.controllers.EventController;
import com.example.slices.databinding.OrganizerEventsFragmentBinding;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * OrganizerEventsFragment.java
 *
 * Purpose: Fragment that displays all events created by the current organizer,
 *          divided into Upcoming, In Progress, and Past sections.
 *          Uses RecyclerViews and OrganizerEventAdapter to display event cards.
 *
 * Outstanding Issues:
 * - getAllFutureEvents() currently placeholder; may need proper filtering by organizer ID.
 * - No error handling for empty lists or null event dates.
 * - Sorting logic is simple right now...later it will be implemented based on event status.
 *
 * @author: Juliana
 */
public class OrganizerEventsFragment extends Fragment {

    // View binding object automatically generated from your XML layout
    private OrganizerEventsFragmentBinding binding;

    // Lists for each section
    private final List<Event> upcomingEvents = new ArrayList<>();
    private final List<Event> inProgressEvents = new ArrayList<>();
    private final List<Event> pastEvents = new ArrayList<>();

    private SharedViewModel vm;




    /**
     * Inflates the fragment layout using view binding.
     *
     * @param inflater  LayoutInflater object.
     * @param container Parent view group.
     * @param savedInstanceState Bundle containing saved state.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout using view binding (organizer_events_fragment.xml)
        binding = OrganizerEventsFragmentBinding.inflate(inflater, container, false);
        // Initialize SharedViewModel
        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    /**
     * Initializes RecyclerViews and loads the organizer's events.
     *
     * @param view The root view.
     * @param savedInstanceState Bundle containing saved state.
     */
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

    /**
     * Loads events from the database and categorizes them as Upcoming, In Progress, or Past.
     * Updates the corresponding RecyclerViews with an OrganizerEventAdapter.
     */
    private void loadOrganizerEvents() {
        //getAllFutureEvents is a placeholder for the actual organizer's events based on ID
        //Toast.makeText(requireContext(), "Organizer ID: " + vm.getUser().getId(), Toast.LENGTH_SHORT).show();
        EventController.getEventsForOrganizer(vm.getUser().getId(), new EventListCallback() {
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
                    EventInfo eventInfo = e.getEventInfo();
                    long eventTime = eventInfo.getEventDate().toDate().getTime();
                    long regDeadline = eventInfo.getRegEnd().toDate().getTime();

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

    /**
     * Cleans up view binding to avoid memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public OrganizerEventsFragmentBinding getBinding() {
        return binding;
    }


//        Feel free to delete but was just leaving it here in case its helpful, was just testing some db queries
//          loadOrganizerEvents()
//                SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
//                EventController.getEventsForOrganizer(svm.getUser().getId(), new EventListCallback() {
//                    @Override
//                    public void onSuccess(List<Event> events) {
//                        pastEvents.clear();
//                        pastEvents.addAll(events);
//                        binding.rvPast.setAdapter(new OrganizerEventAdapter(requireContext(), pastEvents, OrganizerEventsFragment.this));
//
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//
//                    }
//                });
}
