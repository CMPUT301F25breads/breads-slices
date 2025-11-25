package com.example.slices.interfaces;

import android.location.Location;

public interface LocationCallback {
    void onSuccess(Location location);
    void onFailure(Exception e);
}
