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

public class LocationManager {

    public LocationManager() {}

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

}
