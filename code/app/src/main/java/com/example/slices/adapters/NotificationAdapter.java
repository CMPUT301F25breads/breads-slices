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

import com.example.slices.controllers.DBConnector;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.example.slices.models.Notification;
import com.example.slices.models.NotificationType;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: Bhupinder Singh
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private final Context context;
    private final List<Notification> items = new ArrayList<>();
    private final DBConnector db = new DBConnector();

    public NotificationAdapter(Context context) { this.context = context; }

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

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Notification n = items.get(position);

        //Sets the text of the notification card
        h.title.setText(n.getTitle());
        h.body.setText(n.getBody());


        // Tries to fetch the event name and date from the database,
        // if it fails it will set the text to "Event not found"
        db.getEvent(n.getEventId(), new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                h.eventName.setText(event.getName());
                h.eventDate.setText(event.getEventDate().toDate().toString());
            }
            @Override
            public void onFailure(Exception e) {
                h.eventName.setText("Event not found");
                h.eventDate.setText("Event not found");
                Log.e("NotificationAdapter", "Error fetching event", e);
                Toast.makeText(context, "Error: Couldn't load event", Toast.LENGTH_SHORT).show();
            }
        });

        // Sets the button text and visibility based on the notification type
        switch (n.getType()) {
            case INVITATION:
                h.acceptButton.setText("Accept");
                h.declineButton.setVisibility(View.VISIBLE);
                h.acceptButton.setVisibility(View.VISIBLE);
                break;
            case NOT_SELECTED:
                h.acceptButton.setText("Stay Registered");
                h.acceptButton.setVisibility(View.VISIBLE);
                h.declineButton.setVisibility(View.VISIBLE);
                break;
            case NOTIFICATION:
                h.acceptButton.setVisibility(View.GONE);
                h.declineButton.setVisibility(View.GONE);
                break;
            default:
                h.acceptButton.setVisibility(View.GONE);
                h.declineButton.setVisibility(View.GONE);
                break;
        }
    }

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
