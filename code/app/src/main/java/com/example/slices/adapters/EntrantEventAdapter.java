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

import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventActions;
import com.example.slices.SharedViewModel;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/** EntrantEventAdapter
 * This adpater is for entrants (users) joining or leaving events
 * This adapter is used when an event is clicked to join or to leave the event. It changes the text
 * as well as the background colors as per Storyboard illustration (purple BG / white BG with
 * white text / black text, respectively).
 *
 * @author Raj Prasad
 * Note: later, figure out how to engage the local list/set of waitlisted event IDs to decide
 * the button state rather than pure Boolean to set fixed states
 */
public class EntrantEventAdapter extends ArrayAdapter<Event> {
    @Nullable
    private EventActions actions;

    @Nullable
    private SharedViewModel vm;

    public void setViewModel(@Nullable SharedViewModel vm) {
        this.vm = vm;
        notifyDataSetChanged();
    }

    // localized caching for per event waitlist state (by int eventId)
    private final Map<Integer, Boolean> waitlisted = new HashMap<>();

    // set/override state for a single event id
    public void setWaitlisted(int eventId, boolean on) {
        waitlisted.put(eventId, on);
        notifyDataSetChanged();
    }

    // read current state -> defaults to false (not on waitlist)
    private boolean isWaitlisted(int eventId) {
        return Boolean.TRUE.equals(waitlisted.get(eventId));
    }


    public EntrantEventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    public void setActions(@Nullable EventActions actions) {
        this.actions = actions;
    }

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
        DBConnector db = new DBConnector();

        if (event == null){
            return view;
        }
        final int eventId = event.getId(); // ID from firestore DB
        final String eventIdStr = String.valueOf(eventId); // then make it string
        // then, isOn can check truth value
        boolean isOn = (vm != null && vm.isWaitlisted(eventIdStr)) || isWaitlisted(eventId);

        // binding core card views, same as EventAdapter behaviour
        TextView title = view.findViewById(R.id.event_title);
        if (title != null) {
            title.setText(event.getName());
        }
        ImageView image = view.findViewById(R.id.image);
        if (image != null) {
            Glide.with(this.getContext()).load(event.getImageUrl()).into(image);
        }

        // join/leave button (safeguarded if the layout doesnt have it)
        View buttonView = view.findViewById(R.id.btn_event_action);
        if (buttonView instanceof Button) {
            Button actionBtn = (Button) buttonView;

            if (isOn) {
                actionBtn.setText("Leave");
                actionBtn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.
                        getColor(getContext(), android.R.color.white)));
                actionBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.button_purple));
                actionBtn.setTag("leave");
            } else {
                actionBtn.setText("Join");
                actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getContext(), R.color.button_purple)));
                actionBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                actionBtn.setTag("join");
            }

            // toggle color/text on click, notify optional callbacks
            actionBtn.setOnClickListener(v -> {
                boolean isLeave = "leave".equals(actionBtn.getTag());
                if (isLeave) {
                    // switch to "Join"
                    actionBtn.setText("Join");
                    // set background color
                    actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(), R.color.button_purple)));
                    // set text color
                    actionBtn.setTextColor(ContextCompat.getColor(getContext(),
                            android.R.color.white));
                    actionBtn.setTag("join");

                    waitlisted.put(eventId, false);
                    event.removeEntrantFromWaitlist(vm.getUser(), new DBWriteCallback()  {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });

                    if (actions != null) {
                        actions.onLeaveClicked(event);
                    }
                } else {
                    // switch to "Leave"
                    actionBtn.setText("Leave");
                    actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(), android.R.color.white)));
                    actionBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.button_purple));
                    actionBtn.setTag("leave");
                    waitlisted.put(eventId, true);
                    event.addEntrantToWaitlist(vm.getUser(), new DBWriteCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });


                    if (actions != null) {
                        actions.onJoinClicked(event);
                    }
                }
            });
        }

        return view;
    }
}