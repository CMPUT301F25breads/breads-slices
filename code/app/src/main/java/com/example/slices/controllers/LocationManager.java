package com.example.slices.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.slices.interfaces.LocationCallback;

/**
 * A class for requesting user location, getting distances between locations, and constructing them
 * @author Bhupinder Singh
 */
public class LocationManager {

    public LocationManager() {}

    /**
     * Checks if location permissions are granted
     * @param context The application context
     * @return true if either FINE or COARSE location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns the users current location
     * Only attempts to get location if permissions are already granted
     * @param context The application context
     * @param callback Callback to handle success or failure
     */
    public void getUserLocation(Context context, LocationCallback callback) {
        // Check if permissions are granted before attempting to get location
        if (!hasLocationPermission(context)) {
            callback.onFailure(new Exception("Location permissions not granted"));
            return;
        }

        FusedLocationProviderClient fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);

        fusedLocationProvider.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    callback.onSuccess(location);
                } else {
                    // Location is null - could be due to location services disabled,
                    // no location available, or timeout
                    callback.onFailure(new Exception("Unable to obtain location. Please ensure location services are enabled."));
                }
            }
        })
        .addOnFailureListener(e -> {
            // Task failed - could be due to location services disabled, 
            // settings issue, or other system errors
            callback.onFailure(new Exception("Location service error: " + e.getMessage(), e));
        });
    }

    /**
     * Creates a location with the given longitude and latitude.
     * Just an easier way to construct a location
     * @param latitude
     * @param longitude
     * @return
     */
    public Location constructLocation(double latitude, double longitude) {
        Location newLocation = new Location("Location");
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        return newLocation;
    }

    /**
     * Returns the distance between two locations in KM
     * @param location1
     * @param location2
     * @return
     */
    public float getDistanceKm(Location location1, Location location2) {
        return location1.distanceTo(location2)/1000;
    }

}
