package com.example.slices.interfaces;


import com.example.slices.models.Entrant;

import java.util.List;

// Callback interface for handling list of entrants retrieved from the database
public interface EntrantListCallback {
    void onSuccess(List<String> entrants);
    void onFailure(Exception e);
}
