package com.example.slices.interfaces;

/**
 * Interface for notification ID callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface NotificationIDCallback {
    void onSuccess(int id);
    void onFailure(Exception e);

}
