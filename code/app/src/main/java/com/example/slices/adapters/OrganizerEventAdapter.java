package com.example.slices.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.slices.R;
import com.example.slices.fragments.OrganizerEventsFragment;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * OrganizerEventAdapter.java
 *
 * Purpose: Adapter for displaying a list of events in a RecyclerView
 *          in the OrganizerEventsFragment. Handles binding event data
 *          to views, image loading with Glide, and click navigation
 *          to the OrganizerEditEventFragment.
 *
 * @author Juliana
 *
 */

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> eventList;
    private final OrganizerEventsFragment fragment; // Needed for navigation

    /**
     * Constructor for OrganizerEventAdapter.
     *
     * @param context  The context in which the adapter is used.
     * @param eventList The list of Event objects to display.
     * @param fragment  The fragment from which navigation occurs.
     */
    public OrganizerEventAdapter(Context context, List<Event> eventList, OrganizerEventsFragment fragment) {
        this.context = context;
        this.eventList = eventList;
        this.fragment = fragment;
    }

    /**
     * Inflates the layout for an individual event card.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The type of view (unused here).
     * @return A ViewHolder containing the inflated view.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.organizer_event_card, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data from an Event object to the corresponding ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the Event in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        EventInfo eventInfo = event.getEventInfo();


        // Set text fields
        holder.eventName.setText(eventInfo.getName());
        holder.eventDetails.setText("Entrants: " + eventInfo.getCurrentEntrants() + "/" + eventInfo.getMaxEntrants());

        // Format event date/time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        String eventTime = sdf.format(eventInfo.getEventDate().toDate());
        holder.eventDateTime.setText(eventTime + " | " + eventInfo.getAddress());

        // Load image with Glide
        Glide.with(context)
                .load(eventInfo.getImage().getUrl())
                .placeholder(R.drawable.ic_image)
                .into(holder.imgEvent);

        // Simple click listener to navigate to OrganizerEditEventFragment
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventID", String.valueOf(event.getId()));

            navigateToEditEvent(bundle);
        });
    }

    /**
     * Returns the total number of events.
     *
     * @return Size of the event list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class holds references to the views for an individual event item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDetails, eventDateTime;
        ImageView imgEvent;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView The view representing an individual event item.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            eventDetails = itemView.findViewById(R.id.eventDetails);
            eventDateTime = itemView.findViewById(R.id.eventDateTime);
            imgEvent = itemView.findViewById(R.id.imgEvent);
        }
    }

    /**
     * Navigates from OrganizerEventsFragment to OrganizerEditEventFragment.
     *
     * @param bundle Bundle containing the event ID for the selected event.
     */
    private void navigateToEditEvent(Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(fragment);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build();

        navController.navigate(R.id.action_OrganizerEventsFragment_to_OrganizerEditEventFragment, bundle, options);
    }
}
