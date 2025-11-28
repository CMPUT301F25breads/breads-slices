package com.example.slices.interfaces;

import com.example.slices.models.Profile;

import java.util.List;
/**
 * Interface for Profile list callbacks
 * @author Sasieni
 */
public interface ProfileListCallback {
    void onSuccess(List<Profile> profiles);
    void onFailure(Exception e);
}