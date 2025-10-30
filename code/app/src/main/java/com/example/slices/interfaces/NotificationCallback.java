package com.example.slices.interfaces;

import com.example.slices.models.Notification;

public interface NotificationCallback {
    void onSuccess(Notification notification);
    void onFailure(Exception e);
}
