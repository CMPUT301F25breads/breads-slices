package com.example.slices.interfaces;

import com.example.slices.models.Notification;

import java.util.List;

/**
 * Interface for notification list callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface NotificationListCallback {
    void onSuccess(List<Notification> notifications);
    void onFailure(Exception e);
}
