package com.example.slices.interfaces;

/**
 * Interface for entrant ID callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface EntrantIDCallback {
    void onSuccess(int id);
    void onFailure(Exception e);


}
