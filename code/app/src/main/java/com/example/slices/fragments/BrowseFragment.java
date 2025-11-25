package com.example.slices.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.adapters.EntrantEventAdapter;
import com.example.slices.controllers.EventController;
import com.example.slices.models.Event;

import com.example.slices.databinding.BrowseFragmentBinding;
import com.example.slices.interfaces.EventListCallback;
import com.example.slices.models.SearchSettings;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Fragment showing a list of all events
 * Will later also include search functionality
 * @author Brad Erdely, Raj Prasad
 */
public class BrowseFragment extends Fragment {
    private BrowseFragmentBinding binding;
    private ArrayList<Event> eventList = new ArrayList<>();
    private SharedViewModel vm;
    private EntrantEventAdapter eventAdapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = BrowseFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        eventAdapter = new EntrantEventAdapter(requireContext(), BrowseFragment.this, eventList);
        binding.browseList.setLayoutManager(new LinearLayoutManager(requireContext()));
        SearchSettings search = vm.getSearch();

        // Load user's waitlisted events to populate button states correctly
        loadUserWaitlistedEvents();

        setupEvents(search);

        setupListeners();

    }


    public void setupEvents(SearchSettings search) {
        // Set ViewModel on adapter before any data operations to prevent null reference crashes
        eventAdapter.setViewModel(vm);
        binding.browseList.setAdapter(eventAdapter);

        EventController.queryEvents(search, new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                try {
                    eventList.clear();
                    eventList.addAll(events);
                    eventAdapter.notifyDataSetChanged();

                } catch (Exception e) {
                    Log.e("BrowseFragment", "Error setting adapter", e);
                    Toast.makeText(requireContext(), "Error displaying events", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("BrowseFragment", "Error fetching events", e);
                Toast.makeText(requireContext(), "Failed to load events.", Toast.LENGTH_SHORT).show();
                eventAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Setup on item click listeners so clicking an events navigates to event details
     * @author Brad Erdely
     */
    public void setupListeners() {
        binding.searchButton.setOnClickListener(v -> {
            SearchSettings updated = vm.getSearch();
            if(updated == null)
                updated = new SearchSettings();

            updated.setName(binding.searchEditText.getText().toString().trim().toLowerCase());

            vm.setSearch(updated);
            setupEvents(vm.getSearch());
        });
        binding.scanButton.setOnClickListener(v -> {
            navigateToCamera();
        });
        binding.filterButton.setOnClickListener(v -> {
            onFilterClick();
        });
    }

    private void onFilterClick() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_search, null);


        EditText nameInput = dialogView.findViewById(R.id.edit_name);
        nameInput.setText(binding.searchEditText.getText());
        EditText locationInput = dialogView.findViewById(R.id.edit_location);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        // Disable keyboard for date fields
        startDateInput.setInputType(InputType.TYPE_NULL);
        endDateInput.setInputType(InputType.TYPE_NULL);
        startDateInput.setFocusable(false);
        endDateInput.setFocusable(false);


        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Pre-fill current filter values
        SearchSettings current = vm.getSearch();
        if (current != null) {
            if (current.getName() != null)
                nameInput.setText(current.getName());
            if (current.getAddress() != null)
                locationInput.setText(current.getAddress());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            if (current.getAvailStart() != null)
                startDateInput.setText(sdf.format(current.getAvailStart().toDate()));
            if (current.getAvailEnd() != null)
                endDateInput.setText(sdf.format(current.getAvailEnd().toDate()));
        }


        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Search Filters")
                .setView(dialogView)
                .setPositiveButton("Search", (dialog, which) -> {

                    String name = nameInput.getText().toString().trim();
                    String location = locationInput.getText().toString().trim();

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Timestamp startTimestamp = null;
                    Timestamp endTimestamp = null;

                    try {
                        String startStr = startDateInput.getText().toString().trim();
                        String endStr = endDateInput.getText().toString().trim();

                        if (!startStr.isEmpty())
                            startTimestamp = new Timestamp(sdf.parse(startStr));

                        if (!endStr.isEmpty()) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(sdf.parse(endStr));
                            cal.add(Calendar.DAY_OF_MONTH, 1);

                            endTimestamp = new Timestamp(cal.getTime());
                        }

                    } catch (ParseException e) {
                        Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                    }

                    SearchSettings newSearch = new SearchSettings();
                    newSearch.setName(name);
                    newSearch.setAddress(location);
                    newSearch.setAvailStart(startTimestamp);
                    newSearch.setAvailEnd(endTimestamp);

                    vm.setSearch(newSearch);
                    binding.searchEditText.setText(name);

                    setupEvents(newSearch);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    /**
     * Displays a DatePickerDialog for the given EditText.
     *
     * @param target EditText to populate with selected date.
     */
    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> target.setText((month + 1) + "/" + dayOfMonth + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void navigateToCamera() {

    }

    /**
     * Loads the user's waitlisted events and populates the waitlistedEventIds in the ViewModel.
     * This ensures that the browse screen shows correct button states (Join vs Leave).
     */
    private void loadUserWaitlistedEvents() {
        // Only load if user is initialized
        if (vm.getUser() == null || vm.getUser().getId() == 0) {
            return;
        }

        // Check if already loaded to avoid redundant queries
        if (vm.getWaitlistedEvents() != null && !vm.getWaitlistedEvents().isEmpty()) {
            // Already loaded, just sync the IDs
            for (Event event : vm.getWaitlistedEvents()) {
                vm.addWaitlistedId(String.valueOf(event.getId()));
            }
            return;
        }

        // Load from database
        EventController.getEventsForEntrant(vm.getUser(), new com.example.slices.interfaces.EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                vm.setWaitlistedEvents(waitEvents);

                // Populate waitlistedEventIds for button state tracking
                for (Event event : waitEvents) {
                    vm.addWaitlistedId(String.valueOf(event.getId()));
                }

                // Refresh the adapter to update button states
                if (eventAdapter != null) {
                    eventAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail - user can still browse events
                Log.e("BrowseFragment", "Failed to load user's waitlisted events", e);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
