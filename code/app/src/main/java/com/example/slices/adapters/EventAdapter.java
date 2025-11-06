package com.example.slices.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.slices.models.Event;
import com.example.slices.R;
import com.google.firebase.Timestamp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Basic adapter to display important event information
 * Will change to a recycler adapter after halfway
 * @author Brad Erdely
 */
public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.user_events_card, parent, false);
        } else {
            view = convertView;
        }

        Event event = getItem(position);
        TextView title = view.findViewById(R.id.event_title);
        title.setText(event.getName());
        ImageView image = view.findViewById(R.id.image);
        Glide.with(this.getContext()).load(event.getImageUrl()).into(image);
        TextView details = view.findViewById(R.id.event_date_place);
        Date date = event.getEventDate().toDate();

        SimpleDateFormat formatter = new SimpleDateFormat("ha | MMM. dd, yyyy", Locale.CANADA);

        String formatted = formatter.format(date);
        String a = formatted + " | " + event.getLocation();
        details.setText(a);

        return view;
    }
}
