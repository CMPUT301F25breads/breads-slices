package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.models.Entrant;

import java.util.List;

/**
 * RecyclerView Adapter for displaying entrants in a list
 * Used in EventEntrantsFragment to show waitlist entrants
 * @author Saahil
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.ViewHolder> {

    private final Context context;
    private List<Entrant> entrants;

    /**
     * Constructor for EntrantAdapter
     * @param context Android context for inflating layouts
     * @param entrants Initial list of entrants to display
     */
    public EntrantAdapter(Context context, List<Entrant> entrants) {
        this.context = context;
        this.entrants = entrants;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.entrant_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);

        // Handle null/empty name with placeholder
        String name = entrant.getProfile() != null ? entrant.getProfile().getName() : null;
        if (name == null || name.trim().isEmpty()) {
            holder.nameTextView.setText(R.string.anonymous_user);
        } else {
            holder.nameTextView.setText(name);
        }

        // Handle null/empty email with placeholder
        String email = entrant.getProfile() != null ? entrant.getProfile().getEmail() : null;
        if (email == null || email.trim().isEmpty()) {
            holder.emailTextView.setText(R.string.no_email_provided);
        } else {
            holder.emailTextView.setText(email);
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * Updates the adapter with a new list of entrants and refreshes the view
     * @param entrants New list of entrants to display
     */
    public void updateEntrants(List<Entrant> entrants) {
        this.entrants = entrants;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class for entrant list items
     * Holds references to the name and email TextViews
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_entrant_name);
            emailTextView = itemView.findViewById(R.id.tv_entrant_email);
        }
    }
}
