package com.example.slices.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.slices.controllers.ImageController;
import com.example.slices.interfaces.ImageUrlCallback;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.example.slices.models.EventInfo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Recycler Adapter for use with events in the user
 * MyEvents fragment
 * @author Brad Erdely
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final Context context;
    private final List<Event> events;
    private final Fragment fragment; // Reference for NavController

    public EventAdapter(Context context, List<Event> events, Fragment fragment) {
        this.context = context;
        this.events = events;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.user_events_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        EventInfo eventInfo = event.getEventInfo();


        // Set text fields
        holder.title.setText(eventInfo.getName());
        Date date = eventInfo.getEventDate().toDate();
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a | MMM dd, yyyy", Locale.CANADA);
        holder.details.setText(formatter.format(date));
        holder.place.setText(eventInfo.getAddress());

        // Load image
        Glide.with(context)
                .load(eventInfo.getImageUrl())
                .placeholder(R.drawable.ic_image)
                .into(holder.image);

        // Click listener to navigate to EventDetailsFragment
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventID", String.valueOf(event.getId()));

            navigateToDetails(bundle);
        });
    }

    /**
     * Navigates the the EventDetailsFragment
     * @param bundle
     *      bundle containing the eventId of the selected item
     */
    private void navigateToDetails(Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(fragment);
        NavOptions options = new NavOptions.Builder()
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_graph, false)
                .build();

        navController.navigate(R.id.action_global_EventDetailsFragment, bundle, options);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, details, place;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            details = itemView.findViewById(R.id.event_date_place);
            image = itemView.findViewById(R.id.image);
            place = itemView.findViewById(R.id.event_place);
        }
    }
}