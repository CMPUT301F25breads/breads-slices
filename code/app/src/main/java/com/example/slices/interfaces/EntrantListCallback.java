package com.example.slices.interfaces;


import com.example.slices.models.Entrant;

import java.util.List;

/**
 * Interface for entrant list callbacks
 * @author ?
 * @version 1.0
 */
public interface EntrantListCallback {
    void onSuccess(List<Entrant> entrants);
    void onFailure(Exception e);
}
