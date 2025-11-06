package com.example.slices.interfaces;

import com.example.slices.models.Entrant;

/**
 * Interface for entrant callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface EntrantCallback {
    void onSuccess(Entrant entrant);
    void onFailure(Exception e);
}
