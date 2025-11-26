package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.R;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.controllers.NotificationDialog;
import com.example.slices.controllers.NotificationService;
import com.example.slices.databinding.EventEntrantsFragmentBinding;
import com.example.slices.interfaces.EventCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying the list of entrants for a specific event
 * Enhanced with notification capabilities for US 02.05.01, 02.07.01, 02.07.02
 */
public class EventEntrantsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";
    private static final String ARG_SENDER_ID = "sender_id";

    private EventEntrantsFragmentBinding binding;
    private int eventId;
    private String eventName;
    private int senderId;
    private Event currentEvent;

    /**
     * Create a new instance of EventEntrantsFragment with event data
     * @param eventId ID of the event to display entrants for
     * @param eventName Name of the event for display
     * @param senderId ID of the user who will send notifications
     * @return New fragment instance
     */
    public static EventEntrantsFragment newInstance(int eventId, String eventName, int senderId) {
        EventEntrantsFragment fragment = new EventEntrantsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putInt(ARG_SENDER_ID, senderId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Backward compatibility - create instance without sender ID
     */
    public static EventEntrantsFragment newInstance(int eventId, String eventName) {
        return newInstance(eventId, eventName, -1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments passed through navigation
        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID, -1);
            eventName = getArguments().getString(ARG_EVENT_NAME, "Unknown Event");
            senderId = getArguments().getInt(ARG_SENDER_ID, -1);
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

        // Set up notification buttons
        setupNotificationButtons();

        // Set up map button
        setupMapButton();

        // Load event data for notifications
        loadEventData();
    }

    // Update the event information display
    private void updateEventInfo() {
        if (binding != null) {
            binding.tvEventName.setText(eventName);
            binding.tvEntrantCount.setText("Number of entrants");       // Placeholder for now
        }
    }

    // Set up the entrants list container
    private void setupEntrantsContainer() {
        if (binding != null) {
            // Set up RecyclerView with LinearLayoutManager
            binding.recyclerViewEntrants.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
            
            // Show loading state initially
            binding.recyclerViewEntrants.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up notification button click listeners
     * Send notifications to waitlist and participants
     */
    private void setupNotificationButtons() {
        if (binding == null) return;

        // Send to waitlist button
        binding.btnSendToWaitlist.setOnClickListener(v -> {
            if (currentEvent != null && senderId != -1) {
                showNotificationDialog(NotificationType.WAITLIST);
            } else {
                NotificationDialog.showFailureToast(requireContext());
            }
        });

        // Send to participants button
        binding.btnSendToParticipants.setOnClickListener(v -> {
            if (currentEvent != null && senderId != -1) {
                showNotificationDialog(NotificationType.PARTICIPANTS);
            } else {
                NotificationDialog.showFailureToast(requireContext());
            }
        });

        // Initially disable buttons until event is loaded
        binding.btnSendToWaitlist.setEnabled(false);
        binding.btnSendToParticipants.setEnabled(false);
    }

    /**
     * Set up map button to show entrant locations
     */
    private void setupMapButton() {
        if (binding == null) return;

        binding.fabShowMap.setOnClickListener(v -> {
            if (currentEvent == null) {
                android.widget.Toast.makeText(requireContext(),
                        "Event data not loaded yet",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (currentEvent.getEventInfo() == null) {
                android.widget.Toast.makeText(requireContext(),
                        "Event information not available",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check if geolocation is enabled for this event
            if (currentEvent.getEventInfo().getEntrantLoc()) {
                showEntrantMap();
            } else {
                android.widget.Toast.makeText(requireContext(),
                        "Location tracking is not enabled for this event",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // Initially disable and hide map button until event is loaded
        binding.fabShowMap.setEnabled(false);
        binding.fabShowMap.setVisibility(View.GONE);
    }

    /**
     * Navigate to the map fragment to show entrant locations
     */
    private void showEntrantMap() {
        // Use Navigation Component to navigate to EntrantMapFragment
        Bundle args = new Bundle();
        args.putInt("event_id", eventId);
        args.putString("event_name", eventName);
        
        androidx.navigation.Navigation.findNavController(requireView())
                .navigate(R.id.action_EventEntrantsFragment_to_EntrantMapFragment, args);
    }

    // Load event data from database for notification operations
    private void loadEventData() {
        if (eventId == -1) {
            showErrorState("Invalid event ID");
            return;
        }

        com.example.slices.controllers.EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentEvent = event;
                        updateNotificationButtonStates();
                        updateMapButtonVisibility();
                        displayEntrants();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showErrorState("Failed to load event: " + e.getMessage());
                        // Keep buttons disabled if event can't be loaded
                        if (binding != null) {
                            binding.btnSendToWaitlist.setEnabled(false);
                            binding.btnSendToParticipants.setEnabled(false);
                            binding.fabShowMap.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    // Update map button visibility based on event geolocation setting
    private void updateMapButtonVisibility() {
        if (binding == null || currentEvent == null) return;

        // Show map button only if geolocation is enabled for this event
        if (currentEvent.getEventInfo() != null && currentEvent.getEventInfo().getEntrantLoc()) {
            binding.fabShowMap.setVisibility(View.VISIBLE);
            binding.fabShowMap.setEnabled(true);
        } else {
            binding.fabShowMap.setVisibility(View.GONE);
            binding.fabShowMap.setEnabled(false);
        }
    }
    
    // Display entrants in the RecyclerView
    private void displayEntrants() {
        if (binding == null || currentEvent == null) return;
        
        // Get waitlist entrants
        List<Entrant> waitlistEntrants = new ArrayList<>();
        if (currentEvent.getWaitlist() != null && currentEvent.getWaitlist().getEntrants() != null) {
            waitlistEntrants = currentEvent.getWaitlist().getEntrants();
        }
        
        // Update entrant count display
        binding.tvEntrantCount.setText(waitlistEntrants.size() + " entrants on waitlist");
        
        if (waitlistEntrants.isEmpty()) {
            showEmptyState();
        } else {
            // Create and set adapter
            com.example.slices.adapters.EntrantAdapter adapter = 
                new com.example.slices.adapters.EntrantAdapter(requireContext(), waitlistEntrants);
            binding.recyclerViewEntrants.setAdapter(adapter);
            
            // Show RecyclerView, hide other states
            binding.recyclerViewEntrants.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }
    
    // Show empty state when no entrants
    private void showEmptyState() {
        if (binding == null) return;
        binding.recyclerViewEntrants.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.GONE);
    }
    
    // Show error state with message
    private void showErrorState(String message) {
        if (binding == null) return;
        binding.recyclerViewEntrants.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
        binding.tvErrorMessage.setText(message);
    }

    // Update notification button states based on event data
    private void updateNotificationButtonStates() {
        if (binding == null || currentEvent == null || senderId == -1) return;

        // Enable/disable waitlist button based on waitlist size
        boolean hasWaitlistEntrants = currentEvent.getWaitlist() != null &&
                currentEvent.getWaitlist().getEntrants() != null &&
                !currentEvent.getWaitlist().getEntrants().isEmpty();
        binding.btnSendToWaitlist.setEnabled(hasWaitlistEntrants);

        // Enable/disable participants button based on participants size
        boolean hasParticipants = currentEvent.getEntrants() != null &&
                !currentEvent.getEntrants().isEmpty();
        binding.btnSendToParticipants.setEnabled(hasParticipants);
    }

    // Show notification composition dialog
    private void showNotificationDialog(NotificationType type) {
        NotificationDialog.showNotificationDialog(requireContext(), new NotificationDialog.NotificationDialogCallback() {
            @Override
            public void onSendClicked(String title, String message) {
                sendNotification(type, title, message);
            }

            @Override
            public void onCancelClicked() {
                // User cancelled, no action needed
            }
        });
    }

    // Send notification based on type
    private void sendNotification(NotificationType type, String title, String message) {
        if (currentEvent == null || senderId == -1) {
            NotificationDialog.showFailureToast(requireContext());
            return;
        }

        // Get count for success message
        int entrantCount = (type == NotificationType.WAITLIST)
                ? (currentEvent.getWaitlist() != null && currentEvent.getWaitlist().getEntrants() != null 
                    ? currentEvent.getWaitlist().getEntrants().size() : 0)
                : (currentEvent.getEntrants() != null ? currentEvent.getEntrants().size() : 0);

        com.example.slices.interfaces.DBWriteCallback callback = new com.example.slices.interfaces.DBWriteCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            NotificationDialog.showSuccessToast(requireContext(), entrantCount)
                    );
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        NotificationDialog.showFailureToast(requireContext());
                    });
                }
            }
        };

        switch (type) {
            case WAITLIST:
                // Send notification to all entrants on the waitlist
                NotificationService.sendToWaitlist(currentEvent, title, message, eventId, callback);
                break;
            case PARTICIPANTS:
                // Send notification to all entrants in the event
                NotificationService.sendToEventEntrants(currentEvent, title, message, eventId, callback);
                break;
        }
    }

    // Enum for notification types
    private enum NotificationType {
        WAITLIST,
        PARTICIPANTS
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}