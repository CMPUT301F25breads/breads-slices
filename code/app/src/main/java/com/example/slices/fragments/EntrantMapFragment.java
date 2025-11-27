package com.example.slices.fragments;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slices.R;
import com.example.slices.controllers.EventController;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying entrant join locations on a map
 * Implements US 02.02.02 - Show where entrants joined from
 */
public class EntrantMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_NAME = "event_name";

    private int eventId;
    private String eventName;
    private GoogleMap googleMap;
    private Event currentEvent;

    public static EntrantMapFragment newInstance(int eventId, String eventName) {
        EntrantMapFragment fragment = new EntrantMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getInt(ARG_EVENT_ID, -1);
            eventName = getArguments().getString(ARG_EVENT_NAME, "Event");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up toolbar back button
        com.google.android.material.appbar.MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            android.util.Log.d("EntrantMapFragment", "Requesting map async...");
            mapFragment.getMapAsync(this);
        } else {
            android.util.Log.e("EntrantMapFragment", "Map fragment is null!");
            Toast.makeText(getContext(), "Failed to initialize map", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        
        android.util.Log.d("EntrantMapFragment", "Map is ready!");

        // Configure map
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        
        android.util.Log.d("EntrantMapFragment", "Map configured, loading event data...");

        // Load event data and display locations
        loadEventAndDisplayLocations();
    }

    private void loadEventAndDisplayLocations() {
        if (eventId == -1) {
            showError("Invalid event ID");
            return;
        }

        EventController.getEvent(eventId, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentEvent = event;
                        displayEntrantLocations();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Failed to load event: " + e.getMessage());
                    });
                }
            }
        });
    }

    private void displayEntrantLocations() {
        if (currentEvent == null || googleMap == null) {
            android.util.Log.e("EntrantMapFragment", "currentEvent or googleMap is null");
            return;
        }

        android.util.Log.d("EntrantMapFragment", "Event ID: " + currentEvent.getId());
        android.util.Log.d("EntrantMapFragment", "Event name: " + currentEvent.getEventInfo().getName());
        android.util.Log.d("EntrantMapFragment", "Geolocation enabled: " + currentEvent.getEventInfo().getEntrantLoc());

        // Check if geolocation is enabled for this event
        if (!currentEvent.getEventInfo().getEntrantLoc()) {
            android.util.Log.w("EntrantMapFragment", "Location tracking is not enabled for this event");
            showError("Location tracking is not enabled for this event");
            return;
        }

        // Check if waitlist exists
        if (currentEvent.getWaitlist() == null) {
            android.util.Log.e("EntrantMapFragment", "Waitlist is null!");
            showError("No waitlist data available");
            return;
        }

        // Get entrant locations from waitlist
        Map<String, Map<String, Double>> entrantLocations = 
            currentEvent.getWaitlist().getEntrantLocations();

        android.util.Log.d("EntrantMapFragment", "Entrant locations map: " + 
            (entrantLocations == null ? "null" : "size=" + entrantLocations.size()));

        if (entrantLocations == null || entrantLocations.isEmpty()) {
            android.util.Log.w("EntrantMapFragment", "No location data available for entrants");
            showError("No location data available for entrants");
            return;
        }

        // Add markers for each entrant location
        List<LatLng> positions = new ArrayList<>();
        int markerCount = 0;

        android.util.Log.d("EntrantMapFragment", "Processing " + entrantLocations.size() + " entrant location entries");

        for (Map.Entry<String, Map<String, Double>> entry : entrantLocations.entrySet()) {
            String entrantIdStr = entry.getKey();
            Map<String, Double> locationMap = entry.getValue();

            android.util.Log.d("EntrantMapFragment", "Processing entrant ID: " + entrantIdStr);
            android.util.Log.d("EntrantMapFragment", "Location map: " + locationMap);

            if (locationMap != null && 
                locationMap.containsKey("latitude") && 
                locationMap.containsKey("longitude")) {
                
                double lat = locationMap.get("latitude");
                double lng = locationMap.get("longitude");
                LatLng position = new LatLng(lat, lng);

                android.util.Log.d("EntrantMapFragment", "Adding marker at: " + lat + ", " + lng);

                // Convert String ID back to Integer for lookup
                int entrantId;
                try {
                    entrantId = Integer.parseInt(entrantIdStr);
                } catch (NumberFormatException e) {
                    android.util.Log.e("EntrantMapFragment", "Failed to parse entrant ID: " + entrantIdStr);
                    continue;
                }
                
                // Get entrant name if available
                String entrantName = getEntrantName(entrantId);

                // Add marker
                googleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(entrantName)
                        .snippet("Joined from this location"));

                positions.add(position);
                markerCount++;
            } else {
                android.util.Log.w("EntrantMapFragment", "Location map missing lat/lng for entrant: " + entrantIdStr);
            }
        }

        android.util.Log.d("EntrantMapFragment", "Added " + markerCount + " markers to map");

        // Show count
        Toast.makeText(getContext(), 
            "Showing " + markerCount + " entrant location(s)", 
            Toast.LENGTH_SHORT).show();

        // Zoom camera to show all markers
        if (!positions.isEmpty()) {
            android.util.Log.d("EntrantMapFragment", "Zooming to show all markers");
            zoomToShowAllMarkers(positions);
        } else {
            android.util.Log.w("EntrantMapFragment", "No positions to zoom to, staying at default location");
        }
    }

    private String getEntrantName(int entrantId) {
        if (currentEvent == null || currentEvent.getWaitlist() == null) {
            return "Entrant #" + entrantId;
        }

        Entrant entrant = currentEvent.getWaitlist().getEntrant(entrantId);
        if (entrant != null && entrant.getProfile() != null && 
            entrant.getProfile().getName() != null) {
            return entrant.getProfile().getName();
        }

        return "Entrant #" + entrantId;
    }

    private void zoomToShowAllMarkers(List<LatLng> positions) {
        if (positions.isEmpty() || googleMap == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng position : positions) {
            builder.include(position);
        }

        LatLngBounds bounds = builder.build();
        int padding = 100; // padding in pixels

        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (Exception e) {
            // If animation fails, try without animation
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    private void showError(String message) {
        android.util.Log.e("EntrantMapFragment", "Error: " + message);
        
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        
        // Set map to a default location so it's not blank
        if (googleMap != null) {
            LatLng defaultLocation = new LatLng(53.5461, -113.4938); // Edmonton, AB
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
            android.util.Log.d("EntrantMapFragment", "Set map to default location due to error");
        }
        
        // Don't close the fragment - let user see the map and error message
    }
}
