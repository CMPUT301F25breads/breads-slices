package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.R;
import com.example.slices.controllers.EntrantController;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.Invitation;
import com.example.slices.models.Notification;
import com.example.slices.controllers.NotificationDialog;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.controllers.NotificationService;
import com.example.slices.databinding.EventEntrantsFragmentBinding;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.NotificationListCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ListType currentListType = ListType.WAITLIST;

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
            
            // Set up dropdown for list type selection
            setupDropdown();
            
            // Show loading state initially
            binding.recyclerViewEntrants.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    // Set up the dropdown menu for list type selection
    private void setupDropdown() {
        if (binding == null) return;

        // Create dropdown items
        String[] listTypes = new String[]{
                getString(R.string.waiting_list),
                getString(R.string.invited_list),
                getString(R.string.participants_list),
                getString(R.string.cancelled_list)
        };

        // Create adapter for dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                listTypes
        );

        // Set adapter to dropdown
        binding.dropdownListType.setAdapter(adapter);

        // Set default selection
        binding.dropdownListType.setText(getString(R.string.waiting_list), false);

        // Handle dropdown selection
        binding.dropdownListType.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0:
                    currentListType = ListType.WAITLIST;
                    break;
                case 1:
                    currentListType = ListType.INVITED;
                    break;
                case 2:
                    currentListType = ListType.PARTICIPANTS;
                    break;
                case 3:
                    currentListType = ListType.CANCELLED;
                    break;
            }
            // Reload data for the selected list type
            displayEntrantsForCurrentType();
        });
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
                        displayEntrantsForCurrentType();
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
    
    // Display entrants based on current list type
    private void displayEntrantsForCurrentType() {
        if (binding == null || currentEvent == null) return;

        switch (currentListType) {
            case WAITLIST:
                displayWaitlistEntrants();
                break;
            case INVITED:
                displayInvitedEntrants();
                break;
            case PARTICIPANTS:
                displayParticipants();
                break;
            case CANCELLED:
                displayCancelledEntrants();
                break;
        }
    }

    // Display waitlist entrants
    private void displayWaitlistEntrants() {
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

    // Display invited entrants (pending invitations)
    private void displayInvitedEntrants() {
        if (binding == null || currentEvent == null) return;

        // Show loading state
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewEntrants.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);

        // Query invitations for this event
        NotificationManager.getInvitationByEventId(eventId, new NotificationListCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Filter for pending invitations (not accepted, not declined)
                        List<Integer> invitedEntrantIds = new ArrayList<>();
                        for (Notification notification : notifications) {
                            if (notification instanceof Invitation) {
                                Invitation invitation = (Invitation) notification;
                                if (!invitation.isAccepted() && !invitation.isDeclined()) {
                                    invitedEntrantIds.add(invitation.getRecipientId());
                                }
                            }
                        }

                        // Update count display
                        binding.tvEntrantCount.setText(invitedEntrantIds.size() + " invited entrants");

                        if (invitedEntrantIds.isEmpty()) {
                            showEmptyState();
                        } else {
                            // Fetch entrant details for each invited user
                            fetchEntrantsByIds(invitedEntrantIds);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showErrorState("Failed to load invited entrants: " + e.getMessage());
                    });
                }
            }
        });
    }

    // Display confirmed participants
    private void displayParticipants() {
        if (binding == null || currentEvent == null) return;
        
        // Get confirmed participants
        List<Entrant> participants = new ArrayList<>();
        if (currentEvent.getEntrants() != null) {
            participants = currentEvent.getEntrants();
        }
        
        // Update entrant count display
        binding.tvEntrantCount.setText(participants.size() + " confirmed participants");
        
        if (participants.isEmpty()) {
            showEmptyState();
        } else {
            // Create and set adapter
            com.example.slices.adapters.EntrantAdapter adapter = 
                new com.example.slices.adapters.EntrantAdapter(requireContext(), participants);
            binding.recyclerViewEntrants.setAdapter(adapter);
            
            // Show RecyclerView, hide other states
            binding.recyclerViewEntrants.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    // Display cancelled entrants (declined invitations)
    private void displayCancelledEntrants() {
        if (binding == null || currentEvent == null) return;

        // Get cancelled entrant IDs
        List<Integer> cancelledIds = currentEvent.getCancelledIds();
        if (cancelledIds == null) {
            cancelledIds = new ArrayList<>();
        }

        // Update count display
        binding.tvEntrantCount.setText(cancelledIds.size() + " cancelled entrants");

        if (cancelledIds.isEmpty()) {
            showEmptyState();
        } else {
            // Show loading state while fetching entrant details
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.recyclerViewEntrants.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);

            // Fetch entrant details for each cancelled user
            fetchEntrantsByIds(cancelledIds);
        }
    }

    // Helper method to fetch entrants by their IDs
    private void fetchEntrantsByIds(List<Integer> entrantIds) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            showEmptyState();
            return;
        }

        List<Entrant> entrants = new ArrayList<>();
        AtomicInteger fetchedCount = new AtomicInteger(0);
        AtomicInteger totalCount = new AtomicInteger(entrantIds.size());

        for (Integer entrantId : entrantIds) {
            EntrantController.getEntrant(entrantId, new EntrantCallback() {
                @Override
                public void onSuccess(Entrant entrant) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            entrants.add(entrant);
                            
                            // Check if all entrants have been fetched
                            if (fetchedCount.incrementAndGet() == totalCount.get()) {
                                displayEntrantList(entrants);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Still increment count even on failure
                            if (fetchedCount.incrementAndGet() == totalCount.get()) {
                                displayEntrantList(entrants);
                            }
                        });
                    }
                }
            });
        }
    }

    // Helper method to display a list of entrants
    private void displayEntrantList(List<Entrant> entrants) {
        if (binding == null) return;

        if (entrants.isEmpty()) {
            showEmptyState();
        } else {
            // Create and set adapter
            com.example.slices.adapters.EntrantAdapter adapter = 
                new com.example.slices.adapters.EntrantAdapter(requireContext(), entrants);
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

    // Enum for list types
    private enum ListType {
        WAITLIST,
        INVITED,
        PARTICIPANTS,
        CANCELLED
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}