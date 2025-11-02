package com.example.slices.interfaces;

import com.example.slices.models.Entrant;

public interface EntrantCallback {
    void onSuccess(Entrant entrant);
    void onFailure(Exception e);
}
