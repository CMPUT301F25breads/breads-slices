package com.example.slices.models;

/**
 * Enum representing the type of a notification
 * @author Ryan Haubrich
 * @version 1.0
 */
public enum NotificationType {
    /**
     * Notification represents an invitation
     */
    INVITATION,
    /**
     * Notification represents a rejection with the ability to stay registered
     * Added by Bhupinder
     */
    NOT_SELECTED,
    /**
     * Notification represents a general notification
     */
    NOTIFICATION
}
