package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.slices.R;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.google.android.material.button.MaterialButton;


import java.util.ArrayList;
import java.util.List;

/**
 * Used in admin events
 * Loads event details
 * Filters events
 * Removes events
 *
 * @Author Sasieni
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.ViewHolder> {

    private final Context context;
    private final boolean isAdmin;
    private List<Event> eventList;
    private List<Event> eventListFull;

    public AdminEventAdapter(Context context, List<Event> eventList, boolean isAdmin) {
        this.context = context;
        this.eventList = eventList;
        this.eventListFull = new ArrayList<>(eventList);
        this.isAdmin = isAdmin;
    }

    /**
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return new View Holder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_event_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     *Sets title,image and description for event.
     * had remove button
     * @param holder   View holder holding the current items's view
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        EventInfo eventInfo = event.getEventInfo();

        holder.title.setText(eventInfo.getName());
        holder.details.setText(eventInfo.getDescription() != null ? eventInfo.getDescription() : "No description");

        // Safe handling of null image URL or null context
        String imageUrl = eventInfo.getImageUrl();
        Context ctx = holder.itemView.getContext();

        if (ctx == null || holder.icon == null) {
            Log.e("AdminEventAdapter", "Icon is null for event: " + eventInfo.getName());
            return;
        }

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(ctx)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_event_avatar)
                    .error(R.drawable.ic_event_avatar)
                    .into(holder.icon);
        } else {
            // fallback if null
            Glide.with(ctx)
                    .load(R.drawable.ic_event_avatar)
                    .into(holder.icon);
        }

        // Remove button click
        holder.removeButton.setOnClickListener(v -> {
            removeEvent(event);
            Toast.makeText(ctx, eventInfo.getName() + " removed", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Removes event from list and updates the view
     * Still need to edit for DB connection
     * @param event to be removed from list
     */
    private void removeEvent(Event event) {
        eventList.remove(event);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    //Filtering and updates

    /**
     * Filters displayed events based on query
     * if empty, full list is restored
     * @param query
     */
    public void filter(String query) {
        List<Event> filtered = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            filtered.addAll(eventListFull);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Event e : eventListFull) {
                EventInfo eventInfo = e.getEventInfo();
                if (eventInfo.getName().toLowerCase().contains(lowerQuery)
                        || (eventInfo.getLocation() != null && eventInfo.getLocation().toLowerCase().contains(lowerQuery))) {
                    filtered.add(e);
                }
            }
        }
        eventList = filtered;
        notifyDataSetChanged();
    }

    /**
     * Updates list of evens when new data is loaded from Firestore
     * @param newList retrieved from Firestore
     */
    public void updateFullList(List<Event> newList) {
        this.eventList = new ArrayList<>(newList);
        this.eventListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    //ViewHolder

    /**
     * binds xml components to java objects
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, details;
        ImageView icon;
        MaterialButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            details = itemView.findViewById(R.id.eventDetails);
            icon = itemView.findViewById(R.id.eventImage);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}