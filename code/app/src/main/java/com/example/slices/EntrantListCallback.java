package com.example.slices;


import java.util.List;

// Callback interface for handling list of entrants retrieved from the database
public interface EntrantListCallback {
    void onSuccess(List<Entrant> entrants);
    void onFailure(Exception e);
}
