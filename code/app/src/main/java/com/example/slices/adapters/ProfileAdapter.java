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

import com.example.slices.R;
import com.example.slices.controllers.ProfileController;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.Profile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for admin profile list
 */
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<Profile> fullList = new ArrayList<>();
    private List<Profile> displayList = new ArrayList<>();
    private final Context context;

    private boolean showOnlyOrganizers = false;

    public ProfileAdapter(Context context) {
        this.context = context;
    }

    public void setOrganizerMode(boolean enabled) {
        this.showOnlyOrganizers = enabled;
    }

    public void updateList(List<Profile> newList) {
        fullList.clear();
        fullList.addAll(newList);

        displayList.clear();
        if (showOnlyOrganizers) {
            for (Profile p : newList) {
                if (p.isOrganizer()) displayList.add(p);
            }
        } else {
            displayList.addAll(newList);
        }

        notifyDataSetChanged();
    }

    public void filter(String query) {
        displayList.clear();

        if (query == null || query.trim().isEmpty()) {
            if (showOnlyOrganizers) {
                for (Profile p : fullList) {
                    if (p.isOrganizer()) displayList.add(p);
                }
            } else {
                displayList.addAll(fullList);
            }
        } else {
            String lower = query.toLowerCase();

            for (Profile p : fullList) {
                boolean matches = p.getName() != null &&
                        p.getName().toLowerCase().contains(lower);

                if (showOnlyOrganizers) {
                    if (p.isOrganizer() && matches) displayList.add(p);
                } else {
                    if (matches) displayList.add(p);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.admin_profile_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Profile p = displayList.get(position);

        holder.name.setText(p.getName());
        holder.details.setText(p.getEmail() != null ? p.getEmail() : "No email");

        holder.removeBtn.setOnClickListener(v -> {

            ProfileController.deleteProfile(p.getId(), new DBWriteCallback() {
                @Override
                public void onSuccess() {

                    fullList.remove(p);
                    displayList.remove(p);
                    notifyDataSetChanged();

                    Toast.makeText(context, "Profile deleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, details;
        MaterialButton removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.profileName);
            details = itemView.findViewById(R.id.profileDetails);
            removeBtn = itemView.findViewById(R.id.removeButton);
        }
    }
}