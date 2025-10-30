package com.example.slices.interfaces;


import com.example.slices.Event;

import java.util.List;

public interface EventListCallback {
    void onSuccess(List<Event> events);
    void onFailure(Exception e);
}


