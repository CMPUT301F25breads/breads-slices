package com.example.slices.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slices.R;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;

import java.util.List;

/**
 * RecyclerView Adapter for displaying entrants in a list
 * Used in EventEntrantsFragment to show waitlist entrants
 * @author Saahil
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.ViewHolder> {

    /**
     * Callback interface for cancel button clicks
     */
    public interface OnCancelEntrantListener {
        void onCancelEntrant(int entrantId);
    }

    private final Context context;
    private List<Entrant> entrants;
    private Event event;
    private OnCancelEntrantListener cancelListener;

    /**
     * Constructor for EntrantAdapter
     * @param context Android context for inflating layouts
     * @param entrants Initial list of entrants to display
     */
    public EntrantAdapter(Context context, List<Entrant> entrants) {
        this.context = context;
        this.entrants = entrants;
        this.event = null;
        this.cancelListener = null;
    }

    /**
     * Constructor for EntrantAdapter with Event for invited label display
     * @param context Android context for inflating layouts
     * @param entrants Initial list of entrants to display
     * @param event Event object to check invited status
     */
    public EntrantAdapter(Context context, List<Entrant> entrants, Event event) {
        this.context = context;
        this.entrants = entrants;
        this.event = event;
        this.cancelListener = null;
    }

    /**
     * Constructor for EntrantAdapter with Event and cancel listener
     * @param context Android context for inflating layouts
     * @param entrants Initial list of entrants to display
     * @param event Event object to check invited/enrolled status
     * @param cancelListener Listener for cancel button clicks
     */
    public EntrantAdapter(Context context, List<Entrant> entrants, Event event, OnCancelEntrantListener cancelListener) {
        this.context = context;
        this.entrants = entrants;
        this.event = event;
        this.cancelListener = cancelListener;
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

        // Show "Invited" label if entrant is in invitedIds list
        if (event != null && event.getInvitedIds() != null && 
            event.getInvitedIds().contains(entrant.getId())) {
            holder.invitedLabel.setVisibility(View.VISIBLE);
        } else {
            holder.invitedLabel.setVisibility(View.GONE);
        }

        // Show "Cancel" button if entrant is invited but not accepted (non-responsive)
        // Check if entrant has accepted (in event.getEntrantIds())
        if (event != null && event.getInvitedIds() != null && 
            event.getInvitedIds().contains(entrant.getId())) {
            // Entrant is invited
            boolean hasAccepted = event.getEntrantIds() != null && 
                                  event.getEntrantIds().contains(entrant.getId());
            if (!hasAccepted && cancelListener != null) {
                // Show cancel button for non-responsive entrants
                holder.cancelButton.setVisibility(View.VISIBLE);
                holder.cancelButton.setOnClickListener(v -> 
                    cancelListener.onCancelEntrant(entrant.getId())
                );
            } else {
                holder.cancelButton.setVisibility(View.GONE);
            }
        } else {
            holder.cancelButton.setVisibility(View.GONE);
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
     * Holds references to the name, email TextViews, invited label, and cancel button
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView invitedLabel;
        Button cancelButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_entrant_name);
            emailTextView = itemView.findViewById(R.id.tv_entrant_email);
            invitedLabel = itemView.findViewById(R.id.tv_invited_label);
            cancelButton = itemView.findViewById(R.id.btn_cancel_entrant);
        }
    }
}
