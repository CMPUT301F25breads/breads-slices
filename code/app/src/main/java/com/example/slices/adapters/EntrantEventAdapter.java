package com.example.slices.adapters;

import android.content.Context;
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

    private final Context context;
    private final Fragment fragment; // for NavController
    private final List<Event> events;

    @Nullable
    private SharedViewModel vm;

    @Nullable
    private EventActions actions;

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
            details.setText(formatter.format(date) + " | " + eventInfo.getLocation());

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
            } else {
                actionBtn.setVisibility(View.VISIBLE);
            }

            final String eventIdStr = String.valueOf(event.getId());

            // initial waitlist state comes from shared view model
            boolean isOn = vm.isWaitlisted(eventIdStr);
            updateWaitlistButton(actionBtn, isOn);

            // toggle join/leave on click using shared state + controller
            actionBtn.setOnClickListener(v -> {
                // vm and user are already validated above, safe to use here
                boolean isWaitlisted = vm.isWaitlisted(eventIdStr);

                if (isWaitlisted) {
                    // switch to Join
                    updateWaitlistButton(actionBtn, false);

                    EventController.removeEntrantFromWaitlist(event, vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.removeWaitlistedId(eventIdStr);
                            if (actions != null)
                                actions.onLeaveClicked(event);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // reverts on failure
                            updateWaitlistButton(actionBtn, true);
                        }
                    });
                } else {
                    // switch to Leave
                    updateWaitlistButton(actionBtn, true);

                    EventController.addEntrantToWaitlist(event, vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.addWaitlistedId(eventIdStr);
                            if (actions != null)
                                actions.onJoinClicked(event);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // reverts on failure
                            updateWaitlistButton(actionBtn, false);

                            // Show user-friendly error message
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
                    });

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
    }
}