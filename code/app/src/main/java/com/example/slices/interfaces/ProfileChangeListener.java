package com.example.slices.interfaces;

import com.example.slices.models.Profile;

public interface ProfileChangeListener {
    void onProfileChanged(Profile profile, DBWriteCallback callback);
}
