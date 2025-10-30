package com.example.slices.interfaces;

import com.example.slices.models.Notification;

import java.util.List;

public interface NotificationListCallback {
    void onSuccess(List<Notification> notifications);
    void onFailure(Exception e);
}
