package com.example.slices.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Utility class for handling location operations such as
 * retrieving the user's current position and checking whether
 * they are within a required distance of an event.
 */
public class LocationUtils {

    /**
     * Callback used for returning a user's location asynchronously.
     */
    public interface LocationCallback {
        void onLocationResult(Location location);
        void onFailure(String message);
    }

    /**
     * Callback used for distance checks.
     */
    public interface DistanceCallback {
        void onResult(boolean withinDistance, float distanceMeters);
        void onFailure(String message);
    }

    private static FusedLocationProviderClient getClient(Context context) {
        return LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Retrieves the user's last known location asynchronously.
     * Requires location permission to already be granted.
     *
     * @param context  App/activity context
     * @param callback Callback returning location or failure
     */
    @SuppressLint("MissingPermission")
    public static void getUserLocation(Context context, LocationCallback callback) {
        FusedLocationProviderClient client = getClient(context);

        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationResult(location);
                    } else {
                        callback.onFailure("Location unavailable (null).");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Failed to retrieve location."));
    }

    /**
     * Computes distance between two points.
     * @param lat1
     *      Latitude of point 1
     * @param lng1
     *      Longitude of point 1
     * @param lat2
     *      Latitude of point 2
     * @param lng2
     *      Longitude of point 2
     * @return
     *      Distance in meters
     */
    public static float computeDistance(float lat1, float lng1, float lat2, float lng2) {
        Location a = new Location("A");
        a.setLatitude(lat1);
        a.setLongitude(lng1);

        Location b = new Location("B");
        b.setLatitude(lat2);
        b.setLongitude(lng2);

        return a.distanceTo(b);
    }

    /**
     * Checks if user location is within a given distance from the event.
     *
     * @param context
     *      App/activity context
     * @param userLat
     *      User latitude
     * @param userLng
     *      User longitude
     * @param eventLat
     *      Event latitude
     * @param eventLng
     *      Event longitude
     * @param allowedMeters
     *      Maximum allowed distance
     * @param callback
     *      Callback returning result
     */
    public static void checkDistance(
            Context context,
            float userLat,
            float userLng,
            float eventLat,
            float eventLng,
            float allowedMeters,
            DistanceCallback callback) {

        float distance = computeDistance(userLat, userLng, eventLat, eventLng);
        callback.onResult(distance <= allowedMeters, distance);
    }

    /**
     * Convenience method: automatically gets user location
     * then checks if they are within the distance.
     *
     * @param context
     *      App/activity context
     * @param eventLat
     *      Event latitude
     * @param eventLng
     *      Event longitude
     * @param allowedMeters
     *      Maximum allowed distance
     * @param callback
     *      Callback returning result
     */
    @SuppressLint("MissingPermission")
    public static void isUserWithinDistance(
            Context context,
            float eventLat,
            float eventLng,
            float allowedMeters,
            DistanceCallback callback) {

        getUserLocation(context, new LocationCallback() {
            @Override
            public void onLocationResult(Location userLocation) {
                float distance = computeDistance(
                        (float) userLocation.getLatitude(),
                        (float) userLocation.getLongitude(),
                        eventLat,
                        eventLng
                );

                callback.onResult(distance <= allowedMeters, distance);
            }

            @Override
            public void onFailure(String message) {
                callback.onFailure(message);
            }
        });
    }
}
