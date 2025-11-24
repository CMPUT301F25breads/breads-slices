package com.example.slices.adapters;
import com.example.slices.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.slices.controllers.EventController;
import com.example.slices.controllers.NotificationManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.example.slices.models.Invitation;
import com.example.slices.models.Notification;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * RecyclerView adapter that displays Notification items as cards and
 * configures actions based on the NotificationType.
 * For Invitation items, the adapter shows Accept and Decline actions.
 * For Not Selected items, the adapter shows Stay Registered and Decline.
 * For simple Notification items, no actions are shown.
 * Used in NotifFragment
 * @author Bhupinder Singh
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private final Context context;
    private final List<Notification> items = new ArrayList<>();


    /**
     * Creates a new NotificationAdapter.
     * @param context context used for inflating views and UI operations
     */
    public NotificationAdapter(Context context) { this.context = context; }

    /**
     * Replaces the adapter's notifications with the provided list and refreshes the UI.
     * @param list list of notifications to display
     */
    public void setNotifications(List<Notification> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.notification_card, parent, false);
        return new VH(v);
    }

    /**
     * Binds the notification at the given position to the provided ViewHolder.
     * Sets title, body, event details, and configures action buttons
     * based on the notification type.
     * @param h the ViewHolder to bind
     * @param position adapter position of the item
     */
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = items.get(position);

        //Sets the text of the notification card
        h.title.setText(n.getTitle());
        h.body.setText(n.getBody());

        // Format and display the notification timestamp in local timezone
        if (n.getTimestamp() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault()); // Uses device's local timezone
            Date notificationDate = n.getTimestamp().toDate();
            h.eventDate.setText(dateFormat.format(notificationDate));
        } else {
            h.eventDate.setText("Unknown time");
        }

        // Tries to fetch the event name from the database,
        // if it fails it will set the text to "Event not found"
        EventController.getEvent(n.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                EventInfo eventInfo = event.getEventInfo();
                h.eventName.setText(eventInfo.getName());
            }
            @Override
            public void onFailure(Exception e) {
                h.eventName.setText("Event not found");
                Log.e("NotificationAdapter", "Error fetching event", e);
                Toast.makeText(context, "Error: Couldn't load event", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure action buttons based on notification type
        switch (n.getType()) {
            case INVITATION:
                h.acceptButton.setText("Accept");
                h.declineButton.setVisibility(View.VISIBLE);
                h.acceptButton.setVisibility(View.VISIBLE);
                // Ryan changed this
                h.acceptButton.setOnClickListener(v -> {
                    NotificationManager.acceptInvitation((Invitation) n, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            items.remove(n);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Error: Couldn't accept invitation", Toast.LENGTH_SHORT).show();
                            Log.e("NotificationAdapter", "Error accepting invitation", e);
                        }
                    });
                });

                h.declineButton.setOnClickListener(v -> {
                    NotificationManager.declineInvitation((Invitation) n, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            items.remove(n);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Error: Couldn't decline invitation", Toast.LENGTH_SHORT).show();
                            Log.e("NotificationAdapter", "Error declining invitation", e);
                        }
                    });
                });

                break;
            case NOT_SELECTED:
                h.acceptButton.setText("Stay Registered");
                h.acceptButton.setVisibility(View.VISIBLE);
                h.declineButton.setVisibility(View.VISIBLE);
                h.acceptButton.setOnClickListener(v -> {
                    items.remove(n);
                    notifyDataSetChanged();
                });
                // Ryan changed this
                h.declineButton.setOnClickListener(v -> {
                    NotificationManager.declineInvitation((Invitation) n, new DBWriteCallback() {
                        @Override
                        public void onSuccess() {
                            items.remove(n);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(context, "Error: Couldn't decline event", Toast.LENGTH_SHORT).show();
                            Log.e("NotificationAdapter", "Error declining", e);
                        }
                    });
                });
                break;
            case NOTIFICATION:
                h.acceptButton.setVisibility(View.GONE);
                h.declineButton.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Returns the number of notifications in the adapter.
     * @return item count
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for the notification card
     */
    static class VH extends RecyclerView.ViewHolder {
        final TextView title, body, eventName, eventDate;
        final MaterialButton declineButton, acceptButton;
        VH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.notification_card_title);
            body = v.findViewById(R.id.notification_card_body);
            eventName = v.findViewById(R.id.notification_card_event_name);
            eventDate = v.findViewById(R.id.notification_card_event_date);
            declineButton = v.findViewById(R.id.notification_card_decline_button);
            acceptButton = v.findViewById(R.id.notification_card_accept_button);
        }
    }
}
