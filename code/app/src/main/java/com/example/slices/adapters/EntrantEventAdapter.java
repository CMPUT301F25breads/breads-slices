package com.example.slices.adapters;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.controllers.EventController;
import com.example.slices.exceptions.DuplicateEntry;
import com.example.slices.exceptions.WaitlistFull;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventActions;
import com.example.slices.SharedViewModel;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.slices.models.EventInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** EntrantEventAdapter
 * This adapter is for entrants (users) joining or leaving events
 * This adapter is used when an event is clicked to join or to leave the event. It changes the text
 * as well as the background colors as per Storyboard illustration (purple BG / white BG with
 * white text / black text, respectively).
 *
 * @author Raj Prasad
 */
public class EntrantEventAdapter extends RecyclerView.Adapter<EntrantEventAdapter.EventViewHolder> {

    /**
     * Callback interface for requesting location from the fragment
     */
    public interface LocationRequestCallback {
        void onLocationRequested(Event event, JoinWithLocationCallback callback);
    }

    /**
     * Callback interface for receiving location and completing join operation
     */
    public interface JoinWithLocationCallback {
        void onLocationObtained(Location location);
        void onLocationFailed();
    }

    private final Context context;
    private final Fragment fragment; // for NavController
    private final List<Event> events;

    @Nullable
    private SharedViewModel vm;

    @Nullable
    private EventActions actions;

    @Nullable
    private LocationRequestCallback locationRequestCallback;

    /**
     * Constructor for EntrantEventAdapter for displaying events that the entrant can join or leave
     * @param context
     *     the current context
     * @param events
     *      the list of events to display
     */
    public EntrantEventAdapter(Context context, Fragment fragment, List<Event> events) {
        this.context = context;
        this.fragment = fragment;
        this.events = events;
    }

    /**
     * Sets the shared view model used ny this adapter
     * @param vm
     *     the SharedViewModel instance to bind with this adapter
     */
    public void setViewModel(@Nullable SharedViewModel vm) {
        this.vm = vm;
        notifyDataSetChanged();
    }

    /**
     * sets the EventActions interface for handling join/leave actions externally
     * @param actions
     *      the EventActions callback
     */
    public void setActions(@Nullable EventActions actions) {
        this.actions = actions;
    }

    /**
     * Sets the location request callback for handling geolocation-enabled events
     * @param callback
     *      the LocationRequestCallback
     */
    public void setLocationRequestCallback(@Nullable LocationRequestCallback callback) {
        this.locationRequestCallback = callback;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.browse_events_card, parent, false);
        return new EntrantEventAdapter.EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final ImageView image;
        private final Button actionBtn;
        private final TextView details;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            image = itemView.findViewById(R.id.image);
            actionBtn = itemView.findViewById(R.id.btn_event_action);
            details = itemView.findViewById(R.id.event_date_place);
        }

        /**
         * Populate the layout with the information
         * @param event
         *      Event to draw the information from
         */
        private void bind(Event event) {
            if (event == null) return;

            EventInfo eventInfo = event.getEventInfo();

            // Set text fields
            title.setText(eventInfo.getName());
            Date date = eventInfo.getEventDate().toDate();
            SimpleDateFormat formatter = new SimpleDateFormat("ha | MMM. dd, yyyy", Locale.CANADA);
            details.setText(formatter.format(date) + " | " + eventInfo.getAddress());

            // Load image from URL
            Glide.with(context).load(eventInfo.getImageUrl()).into(image);

            // Item click navigates to EventDetailsFragment
            itemView.setOnClickListener(v -> {
                if (vm != null) {
                    vm.setSelectedEvent(event);
                }

                NavController navController = NavHostFragment.findNavController(fragment);
                NavOptions options = new NavOptions.Builder()
                        .setRestoreState(true)
                        .setPopUpTo(R.id.BrowseFragment, true)
                        .build();

                navController.navigate(R.id.action_global_EventDetailsFragment, null, options);
            });

            // Safety check: if vm or user is not properly initialized, we can't determine waitlist status
            if (vm == null || vm.getUser() == null || vm.getUser().getId() == 0) {
                // hide the action button if user is not logged in
                actionBtn.setVisibility(View.GONE);
                return;
            }

            // Check if event has passed - hide button for past events
            if (eventInfo.getEventDate() != null) {
                long eventTimeMillis = eventInfo.getEventDate().toDate().getTime();
                long currentTimeMillis = System.currentTimeMillis();
                
                if (eventTimeMillis < currentTimeMillis) {
                    // Event has passed - hide the join/leave button
                    actionBtn.setVisibility(View.GONE);
                    return;
                }
            }

            // Event is in the future - show the button
            actionBtn.setVisibility(View.VISIBLE);

            final String eventIdStr = String.valueOf(event.getId());

            // initial waitlist state comes from shared view model
            boolean isOn = vm.isWaitlisted(eventIdStr);
            
            updateWaitlistButton(actionBtn, isOn);

            // toggle join/leave on click using shared state + controller
            actionBtn.setOnClickListener(v -> {
                // vm and user are already validated above, safe to use here
                boolean isWaitlisted = vm.isWaitlisted(eventIdStr);

                if (isWaitlisted) {
                    // Disable button and show "Leaving..." state
                    actionBtn.setEnabled(false);
                    actionBtn.setText("Leaving...");

                    EventController.removeEntrantFromWaitlist(event, vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.removeWaitlistedId(eventIdStr);
                            
                            // Only update UI if fragment is still attached
                            if (fragment.isAdded()) {
                                updateWaitlistButton(actionBtn, false);
                                actionBtn.setEnabled(true);
                            }
                            
                            if (actions != null)
                                actions.onLeaveClicked(event);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Only update UI if fragment is still attached
                            if (fragment.isAdded()) {
                                // Revert to "Leave" state on failure
                                updateWaitlistButton(actionBtn, true);
                                actionBtn.setEnabled(true);
                                Toast.makeText(context, "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // Disable button and show "Joining..." state
                    actionBtn.setEnabled(false);
                    actionBtn.setText("Joining...");

                    // Check if event requires geolocation
                    boolean requiresLocation = event.getEventInfo() != null && event.getEventInfo().getEntrantLoc();

                    if (requiresLocation && locationRequestCallback != null) {
                        // Request location from fragment
                        locationRequestCallback.onLocationRequested(event, new JoinWithLocationCallback() {
                            @Override
                            public void onLocationObtained(Location location) {
                                // Join with location - DB call runs even if fragment detaches
                                EventController.addEntrantToWaitlist(event, vm.getUser(), location, new DBWriteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        vm.addWaitlistedId(eventIdStr);
                                        
                                        // Only update UI if fragment is still attached
                                        if (fragment.isAdded()) {
                                            updateWaitlistButton(actionBtn, true);
                                            actionBtn.setEnabled(true);
                                        }
                                        
                                        if (actions != null)
                                            actions.onJoinClicked(event);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // Only update UI if fragment is still attached
                                        if (fragment.isAdded()) {
                                            // Revert to "Join" state on failure
                                            updateWaitlistButton(actionBtn, false);
                                            actionBtn.setEnabled(true);
                                            showJoinError(e);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onLocationFailed() {
                                // Only update UI if fragment is still attached
                                if (fragment.isAdded()) {
                                    // Revert to "Join" state
                                    updateWaitlistButton(actionBtn, false);
                                    actionBtn.setEnabled(true);
                                    Toast.makeText(context, "Location required to join this event", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        // Join without location (non-geolocation event or no callback)
                        EventController.addEntrantToWaitlist(event, vm.getUser(), new DBWriteCallback() {
                            @Override
                            public void onSuccess() {
                                vm.addWaitlistedId(eventIdStr);
                                
                                // Only update UI if fragment is still attached
                                if (fragment.isAdded()) {
                                    updateWaitlistButton(actionBtn, true);
                                    actionBtn.setEnabled(true);
                                }
                                
                                if (actions != null)
                                    actions.onJoinClicked(event);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                // Only update UI if fragment is still attached
                                if (fragment.isAdded()) {
                                    // Revert to "Join" state on failure
                                    updateWaitlistButton(actionBtn, false);
                                    actionBtn.setEnabled(true);
                                    showJoinError(e);
                                }
                            }
                        });
                    }
                }
            });
        }

        /**
         * updateWaitlistButton
         *     updates the background and text color based on waitlist status of the event
         * @param isOn
         *     true if the current user is waitlisted for the event, false if not on waitlist for event
         */
        private void updateWaitlistButton(@NonNull Button button, boolean isOn) {
            if (isOn) {
                button.setText("Leave");
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, android.R.color.white)));
                button.setTextColor(ContextCompat.getColor(context, R.color.button_purple));
                button.setTag("leave");
            } else {
                button.setText("Join");
                button.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(context, R.color.button_purple)));
                button.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                button.setTag("join");
            }
        }

        /**
         * Shows user-friendly error message for join failures
         */
        private void showJoinError(Exception e) {
            String message = "Failed to join waitlist";
            if (e.getMessage() != null) {
                if (e.getMessage().contains("full")) {
                    message = "Waitlist is full";
                } else if (e.getMessage().contains("already")) {
                    message = "You're already on the waitlist";
                } else {
                    message = e.getMessage();
                }
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}