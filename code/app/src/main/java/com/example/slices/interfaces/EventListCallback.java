package com.example.slices.interfaces;


import com.example.slices.models.Event;

import java.util.List;

public interface EventListCallback {
    void onSuccess(List<Event> events);
    void onFailure(Exception e);
}


