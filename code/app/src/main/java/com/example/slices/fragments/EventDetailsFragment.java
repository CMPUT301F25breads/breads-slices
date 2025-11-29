package com.example.slices.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import com.example.slices.controllers.EventController;
import com.example.slices.exceptions.DuplicateEntry;
import com.example.slices.exceptions.WaitlistFull;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.databinding.EventDetailsFragmentBinding;
import com.example.slices.models.EventInfo;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** EventDetailsFragment
 * A fragment for displaying the details of a tapped-on event in the Browse window
 *
 * @author Raj Prasad
 */
public class EventDetailsFragment extends Fragment {
    private EventDetailsFragmentBinding binding;
    private SharedViewModel vm;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    private boolean isWaitlisted = false;
    private Event e;

    /**
     * updateWaitlistButton
     *     updates the waitlist button text color and background based on waitlist status of the event
     * @param isOn
     *     true if the current user is waitlisted for the event, false if not on waitlist for event
     */
    /* TODO: Make the button updates less ugly in EntrantEventAdapter (shouldn't have
        all that code inside an adapter) - Raj
    */
    private void updateWaitlistButton(boolean isOn) {
        if (isOn) {
            binding.btnJoinWaitlist.setText("Leave Waitlist");
            binding.btnJoinWaitlist.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.red)
            );
            binding.btnJoinWaitlist.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        } else {
            binding.btnJoinWaitlist.setText("Join Waitlist");
            binding.btnJoinWaitlist.setBackgroundTintList(
                    ContextCompat.getColorStateList(requireContext(), R.color.button_purple)
            );
            binding.btnJoinWaitlist.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
            );
        }
    }

    // Inflate binding
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = EventDetailsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called after the fragment's view has been created.
     * Initialize event details, set up waitlist button behaviours and bind event date to the UI
     * @param view the fragment's root view
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Set up location permission launcher
        setupLocationPermissionLauncher();

        // Get args and use them if there are any
        // Or just use the SharedViewModel - Brad
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventID")) {
            int eventId = -1;
            try {
                eventId = Integer.parseInt(args.getString("eventID"));
            } catch (NumberFormatException ex) {
                return;
            }


            EventController.getEvent(eventId, new EventCallback() {
                @Override
                public void onSuccess(Event event) {
                    e = event;
                    setupUI();
                }

                @Override
                public void onFailure(Exception e) {
                }
            });

        } else {
            if (vm.getSelectedEvent() != null) {
                e = vm.getSelectedEvent();
                setupUI();
            }
        }
    }

    // nullify binding, go back to previous view
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        // Don't nullify vm - async callbacks may still need it
    }

    /**
     * Sets up the location permission launcher to handle permission request results
     * This must be called in onViewCreated() before any permission requests
     */
    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Check if any location permission was granted
                Boolean fineLocationGranted = permissions.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                
                if ((fineLocationGranted != null && fineLocationGranted) || 
                    (coarseLocationGranted != null && coarseLocationGranted)) {
                    // Permission granted - get location and join waitlist
                    final String eventIdStr = String.valueOf(e.getId());
                    com.example.slices.controllers.LocationManager locationManager = 
                        new com.example.slices.controllers.LocationManager();
                    
                    locationManager.getUserLocation(requireContext(), new com.example.slices.interfaces.LocationCallback() {
                        @Override
                        public void onSuccess(android.location.Location location) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), 
                                    "Location obtained: " + location.getLatitude() + ", " + location.getLongitude(), 
                                    Toast.LENGTH_SHORT).show();
                                joinWaitlistWithLocation(eventIdStr, location);
                            }
                        }
                        
                        @Override
                        public void onFailure(Exception e1) {
                            // Check if fragment is still attached
                            if (!isAdded() || vm == null) return;
                            
                            // Revert on location failure - permissions granted but location unavailable
                            isWaitlisted = false;
                            vm.removeWaitlistedId(eventIdStr);
                            updateWaitlistButton(isWaitlisted);
                            binding.btnJoinWaitlist.setEnabled(true);
                            
                            // Display specific error message
                            String errorMsg = e1.getMessage();
                            if (errorMsg == null || errorMsg.isEmpty()) {
                                errorMsg = "Unable to get your location. Please ensure location services are enabled and try again.";
                            }
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    // Permission denied - revert button state and show error
                    final String eventIdStr = String.valueOf(e.getId());
                    isWaitlisted = false;
                    vm.removeWaitlistedId(eventIdStr);
                    updateWaitlistButton(isWaitlisted);
                    binding.btnJoinWaitlist.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Location permission required to join this event.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    /**
     * Checks if location permissions are granted
     * @return true if either FINE or COARSE location permission is granted
     */
    private boolean hasLocationPermission() {
        return com.example.slices.controllers.LocationManager.hasLocationPermission(requireContext());
    }

    /**
     * Checks location permission state and either gets location immediately or requests permission
     * If permissions are granted: immediately gets location and joins waitlist
     * If permissions are not granted: launches permission request dialog
     */
    private void checkAndRequestLocationPermission() {
        final String eventIdStr = String.valueOf(e.getId());
        
        if (hasLocationPermission()) {
            // Permissions already granted - get location and join waitlist immediately
            com.example.slices.controllers.LocationManager locationManager = 
                new com.example.slices.controllers.LocationManager();
            
            locationManager.getUserLocation(requireContext(), new com.example.slices.interfaces.LocationCallback() {
                @Override
                public void onSuccess(android.location.Location location) {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), 
                            "Location obtained: " + location.getLatitude() + ", " + location.getLongitude(), 
                            Toast.LENGTH_SHORT).show();
                        joinWaitlistWithLocation(eventIdStr, location);
                    }
                }
                
                @Override
                public void onFailure(Exception e1) {
                    // Check if fragment is still attached
                    if (!isAdded() || vm == null) return;
                    
                    // Revert on location failure
                    isWaitlisted = false;
                    vm.removeWaitlistedId(eventIdStr);
                    updateWaitlistButton(isWaitlisted);
                    binding.btnJoinWaitlist.setEnabled(true);
                    
                    // Show helpful error message
                    String errorMsg = "Unable to get your location. Please:\n" +
                                     "1. Enable Location Services in Settings\n" +
                                     "2. Ensure GPS is turned on\n" +
                                     "3. Try again";
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Permissions not granted - launch permission request
            locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void setupUI() {
        if(e == null || vm.getUser() == null)
            return;

        boolean isAdmin = getArguments() != null &&
                getArguments().getBoolean("adminMode", false);


        final String entrantId = String.valueOf(vm.getUser().getId());
        int eventId = e.getId(); // reserved for future join/leave event call usage

        // initial waitlist state based on event data
        isWaitlisted = vm.isWaitlisted(String.valueOf(eventId));

        // update initial button appearance based on waitlist status
        updateWaitlistButton(isWaitlisted);

        // Waitlist button toggling
        binding.btnJoinWaitlist.setOnClickListener(v -> {
            final String eventIdStr = String.valueOf(eventId);

            // checks to see if waitlisted and communicating with DB for join/leave functions
            if (isWaitlisted) {
                // Disable button and show "Leaving..." state
                binding.btnJoinWaitlist.setEnabled(false);
                binding.btnJoinWaitlist.setText("Leaving...");
                
                EventController.removeEntrantFromWaitlist(e, vm.getUser(), new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        // Update ViewModel only after database operation succeeds
                        vm.removeWaitlistedId(eventIdStr);
                        isWaitlisted = false;
                        android.util.Log.d("EventDetailsFragment", "Successfully left waitlist for event " + eventIdStr);
                        
                        // Only update UI if fragment is still attached
                        if (isAdded()) {
                            updateWaitlistButton(isWaitlisted);
                            binding.btnJoinWaitlist.setEnabled(true);
                        }
                        
                        // Refresh the event object from database after successful removal
                        EventController.getEvent(eventId, new EventCallback() {
                            @Override
                            public void onSuccess(Event refreshedEvent) {
                                e = refreshedEvent;
                                android.util.Log.d("EventDetailsFragment", "Event refreshed after leaving waitlist");
                            }

                            @Override
                            public void onFailure(Exception ex) {
                                android.util.Log.e("EventDetailsFragment", "Failed to refresh event after leaving waitlist");
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e1) {
                        // Check if fragment is still attached
                        if (!isAdded() || vm == null) return;
                        
                        // Revert UI state on exception
                        isWaitlisted = true;
                        updateWaitlistButton(isWaitlisted);
                        binding.btnJoinWaitlist.setEnabled(true);
                        
                        // Provide specific error message based on failure type
                        String errorMessage;
                        if (e1.getMessage() != null && e1.getMessage().contains("Entrant not in event")) {
                            errorMessage = "You are not on this waitlist";
                            android.util.Log.i("EventDetailsFragment", "User attempted to leave waitlist they're not on: event " + eventIdStr);
                        } else {
                            errorMessage = "Network error. Please try again.";
                            android.util.Log.e("EventDetailsFragment", "Failed to leave waitlist for event " + eventIdStr + ": " + e1.getMessage());
                        }
                        
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Disable button and show "Joining..." state
                binding.btnJoinWaitlist.setEnabled(false);
                binding.btnJoinWaitlist.setText("Joining...");
                
                // Check if event requires geolocation
                boolean requiresLocation = e.getEventInfo() != null && e.getEventInfo().getEntrantLoc();
                
                if (requiresLocation) {
                    // For geolocation events, check and request permission if needed
                    checkAndRequestLocationPermission();
                } else {
                    // For non-geolocation events, join directly without location
                    joinWaitlistWithLocation(eventIdStr, null);
                }
            }
        });
        EventInfo eventInfo = e.getEventInfo();

        binding.eventTitle.setText(eventInfo.getName());
        binding.eventToolbar.setTitle(eventInfo.getName());
        binding.eventDescription.setText(eventInfo.getDescription());

        // Date/time formatting
        java.util.Date when = (eventInfo.getEventDate() != null) ? eventInfo.getEventDate().toDate() : null;
        String whenText;
        if (when != null) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
                    "h:mm a  |  MMM dd, yyyy", java.util.Locale.getDefault());
            whenText = fmt.format(when);
        } else {
            whenText = "Date/time TBD"; // in case of any errors in date/time, failsafe!
        }
        binding.eventDatetime.setText(whenText);
        binding.eventLocation.setText(e.getEventInfo().getAddress());
        Glide.with(this.getContext()).load(eventInfo.getImage().getUrl()).into(binding.eventImage);

        // counts style reflecting the "Waitlist | Participants" from the xml style
        int wlCount = 0; //waitlist count
        if (e.getWaitlist() != null && e.getWaitlist().getEntrants() != null) {
            wlCount = e.getWaitlist().getEntrants().size();
        }
        int participantCount = 0; // actual participants count
        if (e.getEntrants() != null) {
            participantCount = e.getEntrants().size();
        }
        binding.eventCounts.setText(String.format(java.util.Locale.getDefault(),
                "%d Waitlisted  |  %d Participating", wlCount, participantCount));

        if (isAdmin) {

            // Hide user buttons
            binding.btnGuidelines.setVisibility(View.GONE);
            binding.btnJoinWaitlist.setVisibility(View.GONE);

            // Show middle button (btnDetails in your XML)
            binding.btnBack.setVisibility(View.VISIBLE);

            // Admin back behaviour
            binding.btnBack.setOnClickListener(v ->
                    requireActivity().onBackPressed()
            );

            return;
        }

        binding.btnGuidelines.setOnClickListener(v -> onGuidelinesClicked());

        }

        private void onGuidelinesClicked() {
            new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setMessage(e.getEventInfo().getGuidelines())
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                    })
                    .show();
        }
        
        /**
         * Helper method to join waitlist with or without location
         * @param eventIdStr Event ID as string
         * @param location User's location (can be null if location not required)
         */
        private void joinWaitlistWithLocation(String eventIdStr, android.location.Location location) {
            // Check if fragment is still attached and vm/user are available
            if (!isAdded() || vm == null || vm.getUser() == null) {
                return;
            }
            
            try {
                if (location != null) {
                    // Join with location
                    Toast.makeText(requireContext(), 
                        "Joining waitlist with location...", 
                        Toast.LENGTH_SHORT).show();
                    
                    EventController.addEntrantToWaitlist(e, vm.getUser(), location, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.addWaitlistedId(eventIdStr);
                            isWaitlisted = true;
                            
                            if (isAdded()) {
                                updateWaitlistButton(isWaitlisted);
                                binding.btnJoinWaitlist.setEnabled(true);
                                Toast.makeText(requireContext(), 
                                    "Successfully joined waitlist!", 
                                    Toast.LENGTH_SHORT).show();
                                
                                // Refresh the event object from database after successful join
                                int eventIdInt = Integer.parseInt(eventIdStr);
                                EventController.getEvent(eventIdInt, new EventCallback() {
                                    @Override
                                    public void onSuccess(Event refreshedEvent) {
                                        e = refreshedEvent;
                                        android.util.Log.d("EventDetailsFragment", "Event refreshed after joining waitlist");
                                    }

                                    @Override
                                    public void onFailure(Exception ex) {
                                        android.util.Log.e("EventDetailsFragment", "Failed to refresh event after joining waitlist");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e1) {
                            if (!isAdded() || vm == null) return;
                            isWaitlisted = false;
                            vm.removeWaitlistedId(eventIdStr);
                            updateWaitlistButton(isWaitlisted);
                            binding.btnJoinWaitlist.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Failed to join waitlist. " + e1.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Join without location
                    EventController.addEntrantToWaitlist(e, vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.addWaitlistedId(eventIdStr);
                            isWaitlisted = true;
                            
                            if (isAdded()) {
                                updateWaitlistButton(isWaitlisted);
                                binding.btnJoinWaitlist.setEnabled(true);
                                Toast.makeText(requireContext(), 
                                    "Successfully joined waitlist!", 
                                    Toast.LENGTH_SHORT).show();
                                
                                // Refresh the event object from database after successful join
                                int eventIdInt = Integer.parseInt(eventIdStr);
                                EventController.getEvent(eventIdInt, new EventCallback() {
                                    @Override
                                    public void onSuccess(Event refreshedEvent) {
                                        e = refreshedEvent;
                                        android.util.Log.d("EventDetailsFragment", "Event refreshed after joining waitlist (no location)");
                                    }

                                    @Override
                                    public void onFailure(Exception ex) {
                                        android.util.Log.e("EventDetailsFragment", "Failed to refresh event after joining waitlist (no location)");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e1) {
                            if (!isAdded() || vm == null) return;
                            isWaitlisted = false;
                            vm.removeWaitlistedId(eventIdStr);
                            updateWaitlistButton(isWaitlisted);
                            binding.btnJoinWaitlist.setEnabled(true);
                            Toast.makeText(requireContext(),
                                    "Failed to join waitlist. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            catch (DuplicateEntry e1) {
                if (!isAdded() || vm == null) return;
                isWaitlisted = false;
                vm.removeWaitlistedId(eventIdStr);
                updateWaitlistButton(isWaitlisted);
                Toast.makeText(requireContext(),
                        "You are already on the waitlist for this event.",
                        Toast.LENGTH_SHORT).show();
            }
            catch (WaitlistFull e1) {
                if (!isAdded() || vm == null) return;
                isWaitlisted = false;
                vm.removeWaitlistedId(eventIdStr);
                updateWaitlistButton(isWaitlisted);
                Toast.makeText(requireContext(),
                        "Waitlist is full for this event.",
                        Toast.LENGTH_SHORT).show();
            }
        }

}

