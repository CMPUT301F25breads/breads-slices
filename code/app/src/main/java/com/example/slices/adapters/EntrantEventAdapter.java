package com.example.slices.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.slices.Event;
import com.example.slices.R;
import com.example.slices.interfaces.EventActions;

import java.util.List;
/** EntrantEventAdapter
 * This adpater is for entrants (users) joining or leaving events
 * This adapter is used when an event is clicked to join or to leave the event. It changes the text
 * as well as the background colors as per Storyboard illustration (purple BG / white BG with
 * white text / black text, respectively).
 *
 * @author Raj Prasad
 * Note to Self & Team: Check for any Liskov substitution errors between this and EventAdapter.
 * Note #2: change the code so that it connects directly to DB to check if the event has been joined
 * or not.
 */
public class EntrantEventAdapter extends EventAdapter {
    @Nullable
    private EventActions actions;

    public EntrantEventAdapter(Context context, List<Event> events) {
        super(context, events);
    }

    public void setActions(@Nullable EventActions actions) {
        this.actions = actions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Event event = getItem(position);
        if (event == null) return view;

        // join/leave button color & text color change logic
        View buttonView = view.findViewById(R.id.btn_event_action);
        if (buttonView instanceof Button) {
            Button actionBtn = (Button) buttonView;

            // set the button text and tag the button as "join"
            actionBtn.setText("Join");
            actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getContext(), R.color.button_purple)));
            actionBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            actionBtn.setTag("join");

            actionBtn.setOnClickListener(v -> {
                boolean isLeave = "leave".equals(actionBtn.getTag());
                if (isLeave) {
                    // switch to "Join"
                    actionBtn.setText("Join");
                    actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(), R.color.button_purple)));
                    actionBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                    actionBtn.setTag("join");
                    if (actions != null) actions.onLeaveClicked(event);
                } else {
                    // switch to "Leave"
                    actionBtn.setText("Leave");
                    actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getContext(), android.R.color.white)));
                    actionBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.button_purple));
                    actionBtn.setTag("leave");
                    if (actions != null) actions.onJoinClicked(event);
                }
            });
        }

        return view;
    }
}