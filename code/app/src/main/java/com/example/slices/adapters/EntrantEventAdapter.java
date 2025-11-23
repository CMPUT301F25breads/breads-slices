package com.example.slices.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.slices.controllers.EventController;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventActions;
import com.example.slices.SharedViewModel;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.slices.models.EventInfo;

import java.util.List;

/** EntrantEventAdapter
 * This adapter is for entrants (users) joining or leaving events
 * This adapter is used when an event is clicked to join or to leave the event. It changes the text
 * as well as the background colors as per Storyboard illustration (purple BG / white BG with
 * white text / black text, respectively).
 *
 * @author Raj Prasad
 */
public class EntrantEventAdapter extends ArrayAdapter<Event> {
    @Nullable
    private EventActions actions;

    @Nullable
    private SharedViewModel vm;

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
     * Constructor for EntrantEventAdapter for displaying events that the entrant can join or leave
     * @param context
     *     the current context
     * @param events
     *      the list of events to display
     */
    public EntrantEventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
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
     * updateWaitlistButton
     *     updates the background and text color based on waitlist status of the event
     * @param isOn
     *     true if the current user is waitlisted for the event, false if not on waitlist for event
     */
    private void updateWaitlistButton(@NonNull Button actionBtn, boolean isOn) {
        if (isOn) {
            actionBtn.setText("Leave");
            actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getContext(), android.R.color.white)));
            actionBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.button_purple));
            actionBtn.setTag("leave");
        } else {
            actionBtn.setText("Join");
            actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getContext(), R.color.button_purple)));
            actionBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            actionBtn.setTag("join");
        }
    }

    /**
     * Returns the view for a single vent item in the list
     *
     * @param position the position of the event in the list
     * @param convertView the recycled view to reuse, if available
     * @param parent the parent view that this view will be attached to
     * @return the fully bound view for the given event
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.browse_events_card, parent, false);
        } else {
            view = convertView;
        }
        // inflate the view first, then grab the event + null guard
        Event event = getItem(position);


        if (event == null){
            return view;
        }
        EventInfo eventInfo = event.getEventInfo();
        
        // Safety check: if vm or user is not properly initialized, we can't determine waitlist status
        if (vm == null || vm.getUser() == null || vm.getUser().getId() == 0) {
            // Bind basic event info but hide the action button if user is not logged in
            TextView title = view.findViewById(R.id.event_title);
            if (title != null) {
                title.setText(eventInfo.getName());
            }
            ImageView image = view.findViewById(R.id.image);
            if (image != null) {
                Glide.with(this.getContext()).load(eventInfo.getImageUrl()).into(image);
            }
            
            View buttonView = view.findViewById(R.id.btn_event_action);
            if (buttonView != null) {
                buttonView.setVisibility(View.GONE);
            }
            return view;
        }
        
        final int eventId = event.getId(); // ID from firestore DB
        final String eventIdStr = String.valueOf(eventId); // then make it string
        
        // initial waitlist state comes from shared view model
        boolean isOn = vm.isWaitlisted(eventIdStr);

        // binding core card views, same as EventAdapter behaviour
        TextView title = view.findViewById(R.id.event_title);
        if (title != null) {
            title.setText(eventInfo.getName());
        }
        ImageView image = view.findViewById(R.id.image);
        if (image != null) {
            Glide.with(this.getContext()).load(eventInfo.getImageUrl()).into(image);
        }

        // join/leave button (safeguarded if the layout doesnt have it)
        View buttonView = view.findViewById(R.id.btn_event_action);
        if (buttonView instanceof Button) {
            Button actionBtn = (Button) buttonView;

            updateWaitlistButton(actionBtn, isOn);

            // toggle join/leave on click using shared state + controller
            actionBtn.setOnClickListener(v -> {
                // vm and user are already validated above, safe to use here
                final String userId = String.valueOf(vm.getUser().getId());
                boolean isWaitlisted = vm.isWaitlisted(eventIdStr);

                if (isWaitlisted) {
                    // switch to Join
                    updateWaitlistButton(actionBtn, false);

                    EventController.removeEntrantFromWaitlist(event, vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            vm.removeWaitlistedId(eventIdStr);
                            if (actions != null) {
                                actions.onLeaveClicked(event);
                            }
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
                            if (actions != null) {
                                actions.onJoinClicked(event);
                            }
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
                            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        return view;
    }
}