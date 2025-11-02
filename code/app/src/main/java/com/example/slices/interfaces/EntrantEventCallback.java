package com.example.slices.interfaces;

import com.example.slices.Event;

import java.util.List;

public interface EntrantEventCallback {
    void onSuccess(List<Event> events, List<Event> waitEvents);
    void onFailure(Exception e);
}
