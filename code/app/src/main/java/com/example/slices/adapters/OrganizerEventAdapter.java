package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.slices.R;
import com.example.slices.models.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter to display organizer events inside RecyclerViews.
 * Reuses logic from EventAdapter but uses organizer_event_card.xml.
 *
 * Author: Juliana
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> eventList;

    public OrganizerEventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate organizer_event_card.xml layout for each card
        View view = LayoutInflater.from(context).inflate(R.layout.organizer_event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);

        // Set text fields
        holder.eventName.setText(event.getName());
        holder.eventDetails.setText("Entrants: " + event.getCurrentEntrants() + "/" + event.getMaxEntrants());

        // Format event date/time nicely
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        String eventTime = sdf.format(event.getEventDate().toDate());
        holder.eventDateTime.setText(eventTime + " | " + event.getLocation());

        // Load event image using Glide
        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.ic_image)
                .into(holder.imgEvent);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDetails, eventDateTime;
        ImageView imgEvent;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventDetails = itemView.findViewById(R.id.eventDetails);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            imgEvent = itemView.findViewById(R.id.imgEvent);
        }
    }
}
