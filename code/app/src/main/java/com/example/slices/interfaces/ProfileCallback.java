package com.example.slices.interfaces;

import com.example.slices.models.Profile;

public interface ProfileCallback {
    void onSuccess(Profile profile);
    void onFailure(Exception e);

}
