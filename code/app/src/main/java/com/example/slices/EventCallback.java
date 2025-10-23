package com.example.slices;

public interface EventCallback {
    void onSuccess(Event event);
    void onFailure(Exception e);
}
