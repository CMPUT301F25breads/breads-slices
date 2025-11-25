package com.example.slices.models;

/**
 * Enum representing the type of a log entry
 * @author Ryan Haubrich
 * @version 1.0
 */
public enum LogType {
    /**
     * Log entry is a general notification
     */
    NOTIFICATION_SENT,

    /**
     * Log entry is an invitation
     */
    INVITATION_SENT,

    ENTRANT_JOINED,

    ENTRANT_LEFT,

    ENTRANT_UPDATED,

    EVENT_UPDATED,

    EVENT_DELETED,

    EVENT_CREATED,

    LOTTERY_RUN,

    SYSTEM,

    ERROR,

    INVITATION_ACCEPTED,

    INVITATION_DECLINED,

    WAITLIST_MODIFIED






}
