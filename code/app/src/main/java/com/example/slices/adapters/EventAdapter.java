package com.example.slices.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.slices.models.Event;
import com.example.slices.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
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

        Event event = getItem(position);
        TextView title = view.findViewById(R.id.event_title);
        title.setText(event.getName());
        ImageView image = view.findViewById(R.id.image);
        Glide.with(this.getContext()).load(event.getImageUrl()).into(image);

        /**
         * Added click listener for event card
         */
        
        String formattedDate = "";
        if (event.getEventDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
            formattedDate = sdf.format(event.getEventDate().toDate());
        }

        view.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EventDetailsActivity.class);
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_description", event.getDescription());
            intent.putExtra("event_location", event.getLocation());
            intent.putExtra("event_date", formattedDate);
            intent.putExtra("event_image", event.getImageUrl());
            getContext().startActivity(intent);
        });


        return view;
    }
}
