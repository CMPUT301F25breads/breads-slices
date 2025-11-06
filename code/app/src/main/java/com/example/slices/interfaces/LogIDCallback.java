package com.example.slices.interfaces;

/**
 * Interface for log ID callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface LogIDCallback {
    void onSuccess(int id);
    void onFailure(Exception e);


}
