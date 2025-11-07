package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.models.Entrant;

import java.util.List;

/**
 * Adapter to bind waitlist entrants to RecyclerView cards.
 */
public class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

    private final Context context;
    private final List<Entrant> entrants;

    public WaitlistAdapter(Context context, List<Entrant> entrants) {
        this.context = context;
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.waitlist_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.name.setText(entrant.getName());
        holder.id.setText("ID: " + entrant.getId());
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, id;
        ImageView icon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.waitlistEntrantName);
            id = itemView.findViewById(R.id.waitlistEntrantId);
        }
    }
}