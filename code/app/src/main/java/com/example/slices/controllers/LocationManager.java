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
     * Returns the users current location
     * First checks permissions and requests them if they aren't granted
     * @param context
     * @param callback
     */
    public void getUserLocation(Context context, LocationCallback callback) {
        FusedLocationProviderClient fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            callback.onFailure(new Exception("Permissions not granted for location"));
            return;
        }
        fusedLocationProvider.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    callback.onSuccess(location);
                } else {
                    callback.onFailure(new Exception("Failed to get location"));
                }
            }
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
