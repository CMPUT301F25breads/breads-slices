package com.example.slices.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.adapters.EntrantEventAdapter;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.LocationManager;
import com.example.slices.interfaces.LocationCallback;
import com.example.slices.models.Event;
import com.example.slices.interfaces.EventCallback;

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
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private EntrantEventAdapter.JoinWithLocationCallback pendingJoinCallback;

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

        // Set up location permission launcher
        setupLocationPermissionLauncher();

        // Set up location request callback for adapter
        eventAdapter.setLocationRequestCallback((event, callback) -> {
            pendingJoinCallback = callback;
            checkAndRequestLocationPermission();
        });

        // Set up event actions callback to refresh after leave
        eventAdapter.setActions(new com.example.slices.interfaces.EventActions() {
            @Override
            public void onJoinClicked(Event event) {
                // Refresh user's waitlisted events
                loadUserEvents();
            }

            @Override
            public void onLeaveClicked(Event event) {
                // Refresh user's waitlisted events after leaving
                loadUserEvents();
            }
        });

        // set scanner ready for QR code results
        getParentFragmentManager().setFragmentResultListener("qr_scan_result",
                this, (requestKey, bundle) -> {
            int eventId = bundle.getInt("scanned_event_id", -1);

            if (eventId != -1) {
                openEventDetailsFromQR(eventId);
            }
        });

        // Load user's waitlisted events to populate button states correctly
        loadUserEvents();

        setupEvents(search);

        setupListeners();

    }

    /**
     * openEventDetailsFromQR
     *   called when the CameraFragment returns a scanned EventID from FireStore DB
     *
     *   This method receives the QR code's EventID and redirects to the event using the
     *   EventDetailsFragment
     * @param eventId
     *   The event ID extracted from the QR code
     */
    private void openEventDetailsFromQR(int eventId) {
        EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                // store selected event in viewmodel
                vm.setSelectedEvent(event);

                Log.d("CAMERA", "EVENT NAME: " + event.getEventInfo().getName());

                // navigate to EventDetailsFragment
                goToDetails();


            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(),
                        "Event not found, try again", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void goToDetails() {
        NavController navController = NavHostFragment.findNavController(this);
        NavOptions options = new NavOptions.Builder()
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_graph, false)
                .build();

        navController.navigate(R.id.action_global_EventDetailsFragment, null, options);
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
        CheckBox enrollCheck = dialogView.findViewById(R.id.exclude_check);
        EditText startDateInput = dialogView.findViewById(R.id.edit_start_date);
        EditText endDateInput = dialogView.findViewById(R.id.edit_end_date);

        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Pre-fill current filter values
        SearchSettings current = vm.getSearch();
        if (current != null) {
            if (current.getName() != null)
                nameInput.setText(current.getName());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            if (current.getAvailStart() != null)
                startDateInput.setText(sdf.format(current.getAvailStart().toDate()));
            if (current.getAvailEnd() != null)
                endDateInput.setText(sdf.format(current.getAvailEnd().toDate()));
            enrollCheck.setChecked(current.isEnrolled());
        }


        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Search Filters")
                .setView(dialogView)
                .setPositiveButton("Search", (dialog, which) -> {

                    String name = nameInput.getText().toString().trim();

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
                    newSearch.setAvailStart(startTimestamp);
                    newSearch.setAvailEnd(endTimestamp);
                    newSearch.setId(vm.getUser().getId());
                    newSearch.setCheck(enrollCheck.isChecked());

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

    /**
     * Opens the camera app when tapped
     */
    private void navigateToCamera() {
        NavController navController = NavHostFragment.findNavController(this);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .setRestoreState(true)
                .build();

        navController.navigate(R.id.action_global_cameraFragment, null, options);
    }

    /**
     * Loads the user's events and populates the waitlistedEventIds and participatingEventIds in the ViewModel.
     * This ensures that the browse screen shows correct button states (Join vs Leave).
     */
    private void loadUserEvents() {
        // Only load if user is initialized
        if (vm.getUser() == null || vm.getUser().getId() == 0) {
            Log.d("BrowseFragment", "Skipping loadUserEvents - user not initialized");
            return;
        }

        Log.d("BrowseFragment", "Loading user's events from database");

        // Always load from database to ensure fresh state
        EventController.getEventsForEntrant(vm.getUser(), new com.example.slices.interfaces.EntrantEventCallback() {
            @Override
            public void onSuccess(List<Event> events, List<Event> waitEvents) {
                if (!isAdded()) return;
                
                vm.setWaitlistedEvents(waitEvents);
                vm.setEvents(events);

                // Clear existing waitlisted IDs and rebuild from fresh data
                vm.clearWaitlistedIds();
                vm.clearParticipatingIds();
                
                // Populate waitlistedEventIds for button state tracking
                for (Event event : waitEvents) {
                    vm.addWaitlistedId(String.valueOf(event.getId()));
                }
                for(Event event : events)
                    vm.addParticipatingId(String.valueOf(event.getId()));
                
                Log.d("BrowseFragment", "Loaded " + waitEvents.size() + " waitlisted events");

                // Refresh the adapter to update button states after ViewModel is fully updated
                if (eventAdapter != null) {
                    eventAdapter.notifyDataSetChanged();
                    Log.d("BrowseFragment", "Adapter refreshed with updated button states");
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail - user can still browse events
                Log.e("BrowseFragment", "Failed to load user's events", e);
            }
        });
    }

    /**
     * Sets up the location permission launcher to handle permission request results
     */
    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                Boolean fineLocationGranted = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                
                if ((fineLocationGranted != null && fineLocationGranted) || 
                    (coarseLocationGranted != null && coarseLocationGranted)) {
                    // Permission granted - get location and continue join even if fragment detached
                    getUserLocationAndJoin();
                } else {
                    // Permission denied - notify callback even if fragment detached
                    if (pendingJoinCallback != null) {
                        pendingJoinCallback.onLocationFailed();
                        pendingJoinCallback = null;
                    }
                }
            }
        );
    }

    /**
     * Checks if location permissions are granted
     */
    private boolean hasLocationPermission() {
        return LocationManager.hasLocationPermission(requireContext());
    }

    /**
     * Checks location permission state and either gets location immediately or requests permission
     */
    private void checkAndRequestLocationPermission() {
        if (hasLocationPermission()) {
            // Permissions already granted - get location immediately
            getUserLocationAndJoin();
        } else {
            // Permissions not granted - launch permission request
            locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Gets user location and completes the join operation
     */
    private void getUserLocationAndJoin() {
        if (pendingJoinCallback == null) return;

        // Store callback reference before async operation
        final EntrantEventAdapter.JoinWithLocationCallback callback = pendingJoinCallback;
        pendingJoinCallback = null; // Clear immediately to prevent double-use

        LocationManager locationManager = new LocationManager();
        locationManager.getUserLocation(requireContext(), new LocationCallback() {
            @Override
            public void onSuccess(android.location.Location location) {
                // Always call the callback to complete the join, even if fragment is detached
                // The adapter will handle UI updates only if fragment is still attached
                if (callback != null) {
                    callback.onLocationObtained(location);
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Always call the callback, even if fragment is detached
                if (callback != null) {
                    callback.onLocationFailed();
                }
                
                // Only show toast if fragment is still attached
                if (isAdded()) {
                    Toast.makeText(requireContext(),
                        "Unable to get your location. Please ensure location services are enabled.",
                        Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BrowseFragment", "onResume - refreshing events and waitlist state");
        
        // Refresh events when fragment resumes to ensure button states are current
        SearchSettings search = vm.getSearch();
        EventController.queryEvents(search, new EventListCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                if (!isAdded()) return;
                
                Log.d("BrowseFragment", "Query succeeded with " + events.size() + " events");
                eventList.clear();
                eventList.addAll(events);
                
                // Load user's waitlisted events AFTER event list is updated
                // This ensures proper synchronization between event data and button states
                loadUserEvents();
            }

            @Override
            public void onFailure(Exception e) {
                // On failure, preserve existing state but still try to refresh waitlist
                Log.e("BrowseFragment", "Failed to refresh events on resume", e);
                
                // Still attempt to refresh waitlist state even if event query failed
                if (isAdded()) {
                    loadUserEvents();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        pendingJoinCallback = null;
    }

}
