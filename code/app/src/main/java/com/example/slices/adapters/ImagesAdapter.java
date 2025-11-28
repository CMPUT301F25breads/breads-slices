package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.slices.R;
import com.example.slices.models.Event;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.VH> {

    // Callback to trigger navigation to event details
    public interface OnDetailsClick {
        void onClick(Event event);
    }

    private final Context context;
    private final OnDetailsClick detailsCallback;
    private final List<Event> events = new ArrayList<>();

    public ImagesAdapter(Context context, OnDetailsClick callback) {
        this.context = context;
        this.detailsCallback = callback;
    }

    // Called by fragment to update list
    public void setEvents(List<Event> list) {
        events.clear();
        events.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.images_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = events.get(position);

        // TITLE
        h.title.setText(e.getEventInfo().getName());

        // IMAGE
        Glide.with(context)
                .load(e.getEventInfo().getImageUrl())
                .placeholder(R.drawable.card_background)
                .centerCrop()
                .into(h.img);

        // DETAILS button → navigate to event details
        h.details.setOnClickListener(v -> detailsCallback.onClick(e));

        // REMOVE button (optional)
        h.remove.setOnClickListener(v ->
                Toast.makeText(context,
                        "Remove image not implemented yet",
                        Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        ImageView img;
        TextView title;
        MaterialButton details, remove;

        public VH(@NonNull View v) {
            super(v);

            img = v.findViewById(R.id.image_holder);
            title = v.findViewById(R.id.img_title);
            details = v.findViewById(R.id.btn_details);
            remove = v.findViewById(R.id.btn_remove);
        }
    }
}
