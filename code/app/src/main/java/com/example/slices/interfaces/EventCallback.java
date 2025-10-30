package com.example.slices.interfaces;

import com.example.slices.Event;

public interface EventCallback {
    void onSuccess(Event event);
    void onFailure(Exception e);
}
