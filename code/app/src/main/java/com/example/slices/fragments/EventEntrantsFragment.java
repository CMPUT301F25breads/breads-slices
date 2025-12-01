package com.example.slices.fragments;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    private GestureDetector gestureDetector;
    private boolean hasLotteryBeenDrawn = false;

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

        // Set up swipe gesture detector
        setupSwipeGesture();

        // Set up notification buttons
        setupNotificationButtons();

        // Set up map button
        setupMapButton();

        // Set up download CSV button
        setupDownloadButton();

        // Set up draw replacement button
        setupDrawReplacementButton();

        // Set up cancel all non-responsive button
        setupCancelAllButton();

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

    // Set up swipe gesture detector for switching between lists
    private void setupSwipeGesture() {
        if (binding == null) return;

        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Only allow swipe if lottery has been drawn
                if (!hasLotteryBeenDrawn) {
                    return false;
                }

                if (e1 == null || e2 == null) {
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                // Check if horizontal swipe is dominant
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right - go to previous list
                            switchToPreviousList();
                        } else {
                            // Swipe left - go to next list
                            switchToNextList();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        // Attach gesture detector to multiple areas for better UX
        // 1. Event name/count area (top card)
        binding.tvEventName.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
        
        binding.tvEntrantCount.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    // Switch to the next list type
    private void switchToNextList() {
        switch (currentListType) {
            case WAITLIST:
                currentListType = ListType.INVITED;
                break;
            case INVITED:
                currentListType = ListType.PARTICIPANTS;
                break;
            case PARTICIPANTS:
                currentListType = ListType.CANCELLED;
                break;
            case CANCELLED:
                // Wrap around to waitlist
                currentListType = ListType.WAITLIST;
                break;
        }
        updateDropdownText();
        displayEntrantsForCurrentType();
    }

    // Switch to the previous list type
    private void switchToPreviousList() {
        switch (currentListType) {
            case WAITLIST:
                // Wrap around to cancelled
                currentListType = ListType.CANCELLED;
                break;
            case INVITED:
                currentListType = ListType.WAITLIST;
                break;
            case PARTICIPANTS:
                currentListType = ListType.INVITED;
                break;
            case CANCELLED:
                currentListType = ListType.PARTICIPANTS;
                break;
        }
        updateDropdownText();
        displayEntrantsForCurrentType();
    }

    // Update the dropdown text to match current list type
    private void updateDropdownText() {
        if (binding == null) return;

        String text;
        switch (currentListType) {
            case WAITLIST:
                text = getString(R.string.waiting_list);
                break;
            case INVITED:
                text = getString(R.string.invited_list);
                break;
            case PARTICIPANTS:
                text = getString(R.string.participants_list);
                break;
            case CANCELLED:
                text = getString(R.string.cancelled_list);
                break;
            default:
                text = getString(R.string.waiting_list);
        }
        binding.dropdownListType.setText(text, false);
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
     * Set up download CSV button
     */
    private void setupDownloadButton() {
        if (binding == null) return;

        binding.fabDownloadCsv.setOnClickListener(v -> {
            onExportCSVClicked();
        });

        // Initially hide download button until participants view is shown
        binding.fabDownloadCsv.setVisibility(View.GONE);
    }

    /**
     * Set up draw replacement button
     */
    private void setupDrawReplacementButton() {
        if (binding == null) return;

        binding.btnDrawReplacement.setOnClickListener(v -> {
            onDrawReplacementClicked();
        });

        // Initially hide button until appropriate view is shown
        binding.btnDrawReplacement.setVisibility(View.GONE);
    }

    /**
     * Set up cancel all non-responsive button
     */
    private void setupCancelAllButton() {
        if (binding == null) return;

        binding.btnCancelAllNonResponsive.setOnClickListener(v -> {
            onCancelAllNonResponsiveClicked();
        });

        // Initially hide button until appropriate view is shown
        binding.btnCancelAllNonResponsive.setVisibility(View.GONE);
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
                        
                        // Check if lottery has been drawn (invited list is not empty)
                        hasLotteryBeenDrawn = event.getInvitedIds() != null && !event.getInvitedIds().isEmpty();
                        
                        // Hide dropdown if lottery has been drawn, show swipe hint
                        updateDropdownVisibility();
                        
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

    // Update dropdown visibility based on whether lottery has been drawn
    private void updateDropdownVisibility() {
        if (binding == null) return;

        // Always keep dropdown visible - it's useful for navigation
        // Swipe gestures are just an additional convenience
        binding.dropdownLayout.setVisibility(View.VISIBLE);
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

        // Update download button visibility based on list type
        updateDownloadButtonVisibility();

        // Update draw replacement button visibility based on list type
        updateDrawReplacementButtonVisibility();

        // Update cancel all button visibility based on list type
        updateCancelAllButtonVisibility();

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

    /**
     * Update download button visibility based on current list type
     * Only show download button when viewing participants
     * Download button and map button share the same position - only one shows at a time
     */
    private void updateDownloadButtonVisibility() {
        if (binding == null || currentEvent == null) return;

        // Show download button only when viewing participants list
        if (currentListType == ListType.PARTICIPANTS) {
            // Check if there are enrolled entrants
            boolean hasParticipants = currentEvent.getEntrants() != null && 
                                     !currentEvent.getEntrants().isEmpty();
            if (hasParticipants) {
                binding.fabDownloadCsv.setVisibility(View.VISIBLE);
                binding.fabDownloadCsv.setEnabled(true);
                // Hide map button when download button is shown
                binding.fabShowMap.setVisibility(View.GONE);
            } else {
                binding.fabDownloadCsv.setVisibility(View.GONE);
                // Show map button if geolocation is enabled
                updateMapButtonVisibility();
            }
        } else {
            binding.fabDownloadCsv.setVisibility(View.GONE);
            // Show map button if geolocation is enabled
            updateMapButtonVisibility();
        }
    }

    /**
     * Update draw replacement button visibility and enabled state
     * Show button only when:
     * 1. Viewing waitlist or invited list
     * 2. Registration period has ended (or no reg end date exists)
     * Enable button only when:
     * 1. There are available spots (not all invited entrants have accepted)
     * 2. There are eligible entrants in the waitlist
     */
    private void updateDrawReplacementButtonVisibility() {
        if (binding == null || currentEvent == null) return;

        // Only show on waitlist or invited views
        if (currentListType != ListType.WAITLIST && currentListType != ListType.INVITED) {
            binding.btnDrawReplacement.setVisibility(View.GONE);
            return;
        }

        // Check if registration period has ended
        boolean regPeriodEnded = true; // Default to true if no reg end date
        if (currentEvent.getEventInfo() != null && currentEvent.getEventInfo().getRegEnd() != null) {
            com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();
            regPeriodEnded = now.compareTo(currentEvent.getEventInfo().getRegEnd()) >= 0;
        }

        // Only show button if reg period has ended
        if (!regPeriodEnded) {
            binding.btnDrawReplacement.setVisibility(View.GONE);
            return;
        }

        // Show the button
        binding.btnDrawReplacement.setVisibility(View.VISIBLE);

        // Determine if button should be enabled
        boolean shouldEnable = isDrawReplacementValid();
        binding.btnDrawReplacement.setEnabled(shouldEnable);
        
        // Update button appearance based on enabled state
        if (!shouldEnable) {
            binding.btnDrawReplacement.setAlpha(0.5f);
        } else {
            binding.btnDrawReplacement.setAlpha(1.0f);
        }
    }

    /**
     * Check if draw replacement is valid (should be enabled)
     * Valid when:
     * 1. There are available spots (maxEntrants > current enrolled entrants)
     * 2. There are eligible entrants in waitlist (not already invited or cancelled)
     */
    private boolean isDrawReplacementValid() {
        if (currentEvent == null || currentEvent.getEventInfo() == null) {
            return false;
        }

        // Check if there are available spots
        int maxEntrants = currentEvent.getEventInfo().getMaxEntrants();
        int currentEntrants = currentEvent.getEntrants() != null ? currentEvent.getEntrants().size() : 0;
        int availableSpots = maxEntrants - currentEntrants;

        if (availableSpots <= 0) {
            return false; // Event is full
        }

        // Check if there are eligible entrants in waitlist
        List<Entrant> waitlistEntrants = currentEvent.getWaitlist() != null && 
                                         currentEvent.getWaitlist().getEntrants() != null
                                         ? currentEvent.getWaitlist().getEntrants()
                                         : new ArrayList<>();

        if (waitlistEntrants.isEmpty()) {
            return false; // No one in waitlist
        }

        // Check if there are any eligible entrants (not invited and not cancelled)
        List<Integer> invitedIds = currentEvent.getInvitedIds() != null ? currentEvent.getInvitedIds() : new ArrayList<>();
        List<Integer> cancelledIds = currentEvent.getCancelledIds() != null ? currentEvent.getCancelledIds() : new ArrayList<>();

        for (Entrant entrant : waitlistEntrants) {
            if (!invitedIds.contains(entrant.getId()) && !cancelledIds.contains(entrant.getId())) {
                return true; // Found at least one eligible entrant
            }
        }

        return false; // No eligible entrants
    }

    /**
     * Update cancel all non-responsive button visibility and enabled state
     * Show button only when:
     * 1. Viewing invited list
     * 2. There are non-responsive invited entrants (invited but not accepted)
     */
    private void updateCancelAllButtonVisibility() {
        if (binding == null || currentEvent == null) return;

        // Only show on invited view
        if (currentListType != ListType.INVITED) {
            binding.btnCancelAllNonResponsive.setVisibility(View.GONE);
            return;
        }

        // Check if there are non-responsive entrants (invited but not accepted)
        List<Integer> invitedIds = currentEvent.getInvitedIds() != null ? currentEvent.getInvitedIds() : new ArrayList<>();
        List<Integer> entrantIds = currentEvent.getEntrantIds() != null ? currentEvent.getEntrantIds() : new ArrayList<>();

        // Find non-responsive entrants
        List<Integer> nonResponsiveIds = new ArrayList<>();
        for (Integer invitedId : invitedIds) {
            if (!entrantIds.contains(invitedId)) {
                nonResponsiveIds.add(invitedId);
            }
        }

        // Only show button if there are non-responsive entrants
        if (nonResponsiveIds.isEmpty()) {
            binding.btnCancelAllNonResponsive.setVisibility(View.GONE);
        } else {
            binding.btnCancelAllNonResponsive.setVisibility(View.VISIBLE);
            binding.btnCancelAllNonResponsive.setEnabled(true);
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
        String countText = waitlistEntrants.size() + " entrants on waitlist";
        if (hasLotteryBeenDrawn) {
            countText += " • Swipe here to switch lists";
        }
        binding.tvEntrantCount.setText(countText);
        
        if (waitlistEntrants.isEmpty()) {
            showEmptyState();
        } else {
            // Create and set adapter with event to show "Invited" labels
            com.example.slices.adapters.EntrantAdapter adapter = 
                new com.example.slices.adapters.EntrantAdapter(requireContext(), waitlistEntrants, currentEvent);
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

        // Use invitedIds from the event directly to avoid duplicates
        List<Integer> invitedIds = currentEvent.getInvitedIds();
        if (invitedIds == null) {
            invitedIds = new ArrayList<>();
        }

        // Update count display
        String countText = invitedIds.size() + " invited entrants";
        if (hasLotteryBeenDrawn) {
            countText += " • Swipe here to switch lists";
        }
        binding.tvEntrantCount.setText(countText);

        if (invitedIds.isEmpty()) {
            showEmptyState();
        } else {
            // Fetch entrant details for each invited user
            fetchEntrantsByIdsWithCancelListener(invitedIds);
        }
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
        String countText = participants.size() + " confirmed participants";
        if (hasLotteryBeenDrawn) {
            countText += " • Swipe here to switch lists";
        }
        binding.tvEntrantCount.setText(countText);
        
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
        String countText = cancelledIds.size() + " cancelled entrants";
        if (hasLotteryBeenDrawn) {
            countText += " • Swipe here to switch lists";
        }
        binding.tvEntrantCount.setText(countText);

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

    // Helper method to fetch entrants by their IDs with cancel listener
    private void fetchEntrantsByIdsWithCancelListener(List<Integer> entrantIds) {
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
                                displayEntrantListWithCancelListener(entrants);
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
                                displayEntrantListWithCancelListener(entrants);
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

    // Helper method to display a list of entrants with cancel listener
    private void displayEntrantListWithCancelListener(List<Entrant> entrants) {
        if (binding == null) return;

        if (entrants.isEmpty()) {
            showEmptyState();
        } else {
            // Create and set adapter with event and cancel listener
            com.example.slices.adapters.EntrantAdapter adapter = 
                new com.example.slices.adapters.EntrantAdapter(
                    requireContext(), 
                    entrants, 
                    currentEvent,
                    this::onCancelEntrant
                );
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
        // Set default message based on notification type
        String defaultTitle = null;
        String defaultMessage = null;

        if (type == NotificationType.WAITLIST) {
            // Default message for waitlist notifications
            defaultTitle = "Waitlist Update";
            defaultMessage = "Thanks for joining the waitlist! Stay tuned for updates.";
        }

        NotificationDialog.showNotificationDialog(requireContext(), new NotificationDialog.NotificationDialogCallback() {
            @Override
            public void onSendClicked(String title, String message) {
                sendNotification(type, title, message);
            }

            @Override
            public void onCancelClicked() {
                // User cancelled, no action needed
            }
        }, defaultTitle, defaultMessage);
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

    /**
     * Handler for Draw Replacement button
     * Draws replacement entrants from the waitlist to fill available spots
     */
    private void onDrawReplacementClicked() {
        if (currentEvent == null) {
            android.widget.Toast.makeText(requireContext(),
                    "Event data not loaded yet",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnDrawReplacement.setEnabled(false);
        }

        // Call EventController.doReplacementLottery()
        com.example.slices.controllers.EventController.doReplacementLottery(currentEvent, new com.example.slices.interfaces.DBWriteCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Hide loading indicator
                        if (binding != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        // Show success message
                        android.widget.Toast.makeText(requireContext(),
                                "Replacement lottery completed successfully!",
                                android.widget.Toast.LENGTH_SHORT).show();

                        // Reload event data to refresh UI and button state
                        loadEventData();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Hide loading indicator
                        if (binding != null) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnDrawReplacement.setEnabled(true);
                        }

                        // Show error message
                        String errorMessage = "Failed to draw replacements.";
                        if (e.getMessage() != null) {
                            if (e.getMessage().contains("No eligible entrants")) {
                                errorMessage = "No eligible entrants remain in the waitlist.";
                            } else if (e.getMessage().contains("full")) {
                                errorMessage = "Event is full. No spots available.";
                            }
                        }

                        android.widget.Toast.makeText(requireContext(),
                                errorMessage,
                                android.widget.Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    /**
     * Handler for Cancel All Non-Responsive button
     * Cancels all invited entrants who have not accepted their invitation
     */
    private void onCancelAllNonResponsiveClicked() {
        if (currentEvent == null) {
            android.widget.Toast.makeText(requireContext(),
                    "Event data not loaded yet",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Find all non-responsive entrants (invited but not accepted)
        List<Integer> invitedIds = currentEvent.getInvitedIds() != null ? currentEvent.getInvitedIds() : new ArrayList<>();
        List<Integer> entrantIds = currentEvent.getEntrantIds() != null ? currentEvent.getEntrantIds() : new ArrayList<>();

        List<Integer> nonResponsiveIds = new ArrayList<>();
        for (Integer invitedId : invitedIds) {
            if (!entrantIds.contains(invitedId)) {
                nonResponsiveIds.add(invitedId);
            }
        }

        if (nonResponsiveIds.isEmpty()) {
            android.widget.Toast.makeText(requireContext(),
                    "No non-responsive entrants to cancel.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel All Non-Responsive Entrants")
                .setMessage("Are you sure you want to cancel " + nonResponsiveIds.size() +
                           " non-responsive entrants? They will be notified that their invitation has expired.")
                .setPositiveButton("Cancel Them", (dialog, which) -> {
                    // Show loading indicator
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.btnCancelAllNonResponsive.setEnabled(false);
                    }

                    // Call EventController.cancelMultipleEntrants()
                    com.example.slices.controllers.EventController.cancelMultipleEntrants(
                            currentEvent,
                            nonResponsiveIds,
                            new com.example.slices.interfaces.DBWriteCallback() {
                                @Override
                                public void onSuccess() {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            // Hide loading indicator
                                            if (binding != null) {
                                                binding.progressBar.setVisibility(View.GONE);
                                            }

                                            // Show success message
                                            android.widget.Toast.makeText(requireContext(),
                                                    "Successfully cancelled " + nonResponsiveIds.size() + " non-responsive entrants.",
                                                    android.widget.Toast.LENGTH_SHORT).show();

                                            // Reload event data to refresh UI
                                            loadEventData();
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            // Hide loading indicator
                                            if (binding != null) {
                                                binding.progressBar.setVisibility(View.GONE);
                                                binding.btnCancelAllNonResponsive.setEnabled(true);
                                            }

                                            // Show error message
                                            String errorMessage = "Failed to cancel entrants.";
                                            if (e.getMessage() != null) {
                                                errorMessage = "Failed to cancel entrants: " + e.getMessage();
                                            }

                                            android.widget.Toast.makeText(requireContext(),
                                                    errorMessage,
                                                    android.widget.Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }
                            }
                    );
                })
                .setNegativeButton("Keep Them", null)
                .show();
    }

    /**
     * Handler for Export CSV action
     * Exports enrolled entrants to a CSV file
     */
    private void onExportCSVClicked() {
        if (currentEvent == null) {
            android.widget.Toast.makeText(requireContext(),
                    "Event data not loaded yet",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if enrolled entrants exist
        List<Entrant> enrolledEntrants = currentEvent.getEntrants();
        if (enrolledEntrants == null || enrolledEntrants.isEmpty()) {
            android.widget.Toast.makeText(requireContext(),
                    "No enrolled entrants to export.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading indicator
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        
        // Call EventController.exportEntrantsToCSV()
        com.example.slices.controllers.EventController.exportEntrantsToCSV(
            currentEvent, 
            requireContext(), 
            new com.example.slices.interfaces.CSVExportCallback() {
                @Override
                public void onSuccess(android.net.Uri fileUri) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Hide loading indicator
                            if (binding != null) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                            
                            // Open share dialog with file URI
                            showCSVShareDialog(fileUri);
                        });
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Hide loading indicator
                            if (binding != null) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                            
                            // Display error toast
                            String errorMessage = "Failed to export CSV.";
                            if (e.getMessage() != null) {
                                if (e.getMessage().contains("No enrolled entrants")) {
                                    errorMessage = "No enrolled entrants to export.";
                                } else if (e.getMessage().contains("storage") || e.getMessage().contains("permission")) {
                                    errorMessage = "Failed to create CSV file. Check storage permissions.";
                                }
                            }
                            
                            android.widget.Toast.makeText(requireContext(),
                                    errorMessage,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        );
    }

    /**
     * Shows Android's share dialog for the CSV file
     * 
     * @param fileUri URI of the CSV file to share
     */
    private void showCSVShareDialog(android.net.Uri fileUri) {
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        android.content.Intent chooser = android.content.Intent.createChooser(shareIntent, "Export Entrants CSV");
        startActivity(chooser);
    }

    /**
     * Handler for cancelling a single non-responsive entrant
     * 
     * @param entrantId ID of the entrant to cancel
     */
    private void onCancelEntrant(int entrantId) {
        if (currentEvent == null) {
            android.widget.Toast.makeText(requireContext(),
                    "Event data not loaded yet",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading indicator
        if (binding != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        
        // Call EventController.cancelSingleEntrant()
        com.example.slices.controllers.EventController.cancelSingleEntrant(
            currentEvent, 
            entrantId, 
            new com.example.slices.interfaces.DBWriteCallback() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Hide loading indicator
                            if (binding != null) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                            
                            // Show success message
                            android.widget.Toast.makeText(requireContext(),
                                    "Entrant cancelled successfully.",
                                    android.widget.Toast.LENGTH_SHORT).show();
                            
                            // Refresh entrant lists after success
                            loadEventData();
                        });
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Hide loading indicator
                            if (binding != null) {
                                binding.progressBar.setVisibility(View.GONE);
                            }
                            
                            // Display appropriate error message
                            String errorMessage = "Failed to cancel entrant.";
                            if (e.getMessage() != null) {
                                if (e.getMessage().contains("not found") || e.getMessage().contains("not invited")) {
                                    errorMessage = "Entrant not found or already cancelled.";
                                } else if (e.getMessage().contains("already accepted") || e.getMessage().contains("invalid state")) {
                                    errorMessage = "Cannot cancel: entrant has already accepted invitation.";
                                }
                            }
                            
                            android.widget.Toast.makeText(requireContext(),
                                    errorMessage,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}