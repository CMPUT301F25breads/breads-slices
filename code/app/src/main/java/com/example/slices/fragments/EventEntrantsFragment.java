package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.slices.models.Event;
import com.example.slices.controllers.NotificationDialog;
import com.example.slices.controllers.NotificationService;
import com.example.slices.databinding.EventEntrantsFragmentBinding;
import com.example.slices.interfaces.EventCallback;

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
            // Show the entrants RecyclerView and hide other states
            binding.recyclerViewEntrants.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.layoutErrorState.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
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

    // Load event data from database for notification operations
    private void loadEventData() {
        if (eventId == -1) return;

        com.example.slices.controllers.EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentEvent = event;
                        updateNotificationButtonStates();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Keep buttons disabled if event can't be loaded
                        if (binding != null) {
                            binding.btnSendToWaitlist.setEnabled(false);
                            binding.btnSendToParticipants.setEnabled(false);
                        }
                    });
                }
            }
        });
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
                ? NotificationService.getWaitlistEntrantCount(currentEvent)
                : NotificationService.getEventEntrantCount(currentEvent);

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
                NotificationService.sendToWaitlist(currentEvent, title, message, senderId, callback);
                break;
            case PARTICIPANTS:
                // Send notification to all entrants in the event
                NotificationService.sendToEventEntrants(currentEvent, title, message, senderId, callback);
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