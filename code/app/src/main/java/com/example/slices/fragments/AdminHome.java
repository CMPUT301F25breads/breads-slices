package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.slices.R;
import com.google.android.material.card.MaterialCardView;

/** Home Fragment for Administrator
 * @author Sasieni
 */

public class AdminHome extends Fragment {

    private MaterialCardView btnBrowseEvents;
    private MaterialCardView btnBrowseProfiles;
    private MaterialCardView btnBrowseImages;
    private MaterialCardView btnBrowseOrganizers;
    private MaterialCardView btnLogs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the admin_home.xml layout
        return inflater.inflate(R.layout.admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI components
        btnBrowseEvents = view.findViewById(R.id.btn_browse_events);
        btnBrowseProfiles = view.findViewById(R.id.btn_browse_profiles);
        btnBrowseImages = view.findViewById(R.id.btn_browse_images);
        btnBrowseOrganizers = view.findViewById(R.id.btn_browse_organizers);
        btnLogs = view.findViewById(R.id.btn_logs);

        // Set click listeners
        btnBrowseEvents.setOnClickListener(v -> navigateToFragment(view, R.id.adminEventsFragment));
        btnBrowseProfiles.setOnClickListener(v -> navigateToFragment(view, R.id.adminProfilesFragment));
        btnBrowseImages.setOnClickListener(v -> navigateToFragment(view, R.id.adminImagesFragment));
        btnBrowseOrganizers.setOnClickListener(v -> navigateToFragment(view, R.id.adminOrganizersFragment));
        btnLogs.setOnClickListener(v -> navigateToFragment(view, R.id.adminNotificationsFragment));
    }

    /**
     * Navigates to another fragment in the nav graph.
     */
    private void navigateToFragment(View view, int destinationId) {
        try {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(destinationId);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}