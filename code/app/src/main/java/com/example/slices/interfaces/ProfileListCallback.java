package com.example.slices.interfaces;

import com.example.slices.models.Profile;

import java.util.List;

public interface ProfileListCallback {
    void onSuccess(List<Profile> profiles);
    void onFailure(Exception e);
}