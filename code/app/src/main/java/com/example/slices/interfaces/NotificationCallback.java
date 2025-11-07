package com.example.slices.interfaces;

import com.example.slices.models.Notification;

/**
 * Interface for notification callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface NotificationCallback {
    void onSuccess(Notification notification);
    void onFailure(Exception e);
}
