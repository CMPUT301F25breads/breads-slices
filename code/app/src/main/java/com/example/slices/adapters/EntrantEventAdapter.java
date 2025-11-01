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

import com.example.slices.Event;
import com.example.slices.R;
import com.example.slices.interfaces.EventActions;

import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.slices.models.Waitlist;

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

    /* TODO (Waitlist Status): Local caching of event IDs the user is waitlisted on will go here
    /* Example:
    * private final string Set<String> waitlistedEventIds = new HashSet<>();
    * then call the hosting fragment after it loads the IDs (from DB or local)
    * public void setWaitlisted Ids(@NonNull Collection<Strings> ids) {
    * waitlisted EventIds.clear();
    * waitlistedEventIds.addAll(ids);
    * notifyDataSetChanged(); }
     */

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
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_card, parent, false);
        } else {
            view = convertView;
        }
        // inflate the view first, then grab the event + null guard
        Event event = getItem(position);

        if (event == null) return view;

        // binding core card views, same as EventAdapter behaviour
        TextView title = view.findViewById(R.id.event_title);
        if (title != null) {
            title.setText(event.getName());
        }
        ImageView image = view.findViewById(R.id.image);
        if (image != null) {
            Glide.with(getContext()).load(event.getImageUrl()).into(image);
        }

        // join/leave button (safeguarded if the layout doesnt have it)
        View buttonView = view.findViewById(R.id.btn_event_action);
        if (buttonView instanceof Button) {
            Button actionBtn = (Button) buttonView;

            // set the button text and tag the button as "join" AS DEFAULT (TO BE CHANGED:
            // TODO(Waitlist Status): Before applying default join state, check local IDs list
            actionBtn.setText("Join");
            actionBtn.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getContext(), R.color.button_purple)));
            actionBtn.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
            actionBtn.setTag("join");
            // TODO: (Waitlist Status): remove eventID from local IDs list here (if present)
            // eg., waitlistedEventIds.remove(eventId); notifyDataSetChanged();

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
                    // TODO(Waitlist Status): add eventId to local IDs list here too
                    // e.g., waitlistedEventIds.add(eventId); notifydatasetchanged();
                    if (actions != null) actions.onJoinClicked(event);
                }
            });
        }

        return view;
    }
}