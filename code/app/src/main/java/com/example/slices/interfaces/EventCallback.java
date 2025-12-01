package com.example.slices.interfaces;

import com.example.slices.models.Event;

/**
 * Interface for event callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface EventCallback {
    void onSuccess(Event event);
    default void onFailure(Exception e) {
        // Optional override for failure handling
    }
}
