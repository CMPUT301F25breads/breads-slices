package com.example.slices.interfaces;


import com.example.slices.models.Event;

import java.util.List;

/**
 * Interface for event list callbacks
 * @author ?
 * @version 1.0
 */
public interface EventListCallback {
    void onSuccess(List<Event> events);
    void onFailure(Exception e);
}


