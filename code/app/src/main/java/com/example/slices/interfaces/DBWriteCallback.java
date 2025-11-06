package com.example.slices.interfaces;

/**
 * Interface for database write callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface DBWriteCallback {
    void onSuccess();
    void onFailure(Exception e);
}
