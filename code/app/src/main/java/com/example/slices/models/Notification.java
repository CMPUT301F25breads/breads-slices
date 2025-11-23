package com.example.slices.models;

import com.google.firebase.Timestamp;


/**
 * Class representing a general notification
 * @author Ryan Haubrich
 * @version 1.0
 */
public class Notification {
    /**
     * Title of the notification
     */
    protected String title;

    /**
     * Body text of the notification
     */
    protected String body;

    /**
     * ID of the notification
     */
    protected String id;

    /**
     * ID of the recipient entrant
     */
    protected int recipientId;

    /**
     * ID of the sender entrant
     */
    protected int senderId;

    /**
     * Whether the notification has been read
     */
    protected boolean read;

    /**
     * Timestamp of when the notification was created
     */
    protected Timestamp timestamp;

    /**
     * Type of the notification
     */
    protected NotificationType type;

    /**
     * ID of the event associated with the notification
     */
    protected int eventId;

    /**
     * Default constructor for Firebase
     */
    public Notification() {}

    /**
     * Constructor for Notification
     * @param title
     *      Title of the notification
     * @param body
     *      Body text of the notification
     * @param notificationId
     *      ID of the notification
     * @param recipientId
     *      ID of the recipient entrant
     * @param senderId
     *      ID of the sender entrant
     */
    public Notification(String title, String body, String notificationId, int recipientId, int senderId) {
        this.title = title;
        this.body = body;
        this.id = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.NOTIFICATION;
    }

    /**
     * Getter for the title of the notification
     * @return
     *      Title of the notification
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the body of the notification
     * @return
     *      Body text of the notification
     */
    public String getBody() {
        return body;
    }

    /**
     * Getter for the ID of the notification
     * @return
     *      ID of the notification
     */
    public String getId() {
        return id;
    }

    /**
     * Getter for the recipient ID
     * @return
     *      ID of the recipient entrant
     */
    public int getRecipientId() {
        return recipientId;
    }

    /**
     * Getter for the sender ID
     * @return
     *      ID of the sender entrant
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Getter for the timestamp of the notification
     * @return
     *      Timestamp of when the notification was created
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the type of the notification
     * @return
     *      Type of the notification
     */
    public NotificationType getType() {
        return type;
    }

    /**
     * Getter for the event ID
     * @return
     *      ID of the associated event
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Getter for the read status
     * @return
     *      True if the notification has been read, false otherwise
     */
    public boolean getRead() {
        return read;
    }

    /**
     * Setter for the notification ID
     * @param id
     *      ID to set for the notification
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Setter for the title of the notification
     * @param title
     *      Title to set for the notification
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Setter for the body of the notification
     * @param body
     *      Body text to set for the notification
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Setter for the recipient ID
     * @param recipientId
     *      ID of the recipient entrant
     */
    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * Setter for the read status
     * @param read
     *      Whether the notification has been read
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Setter for the sender ID
     * @param senderId
     *      ID of the sender entrant
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    /**
     * Setter for the event ID
     * @param eventId
     *      ID of the associated event
     */
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    /**
     * Setter for the notification type
     * @param type
     *      Type to set for the notification
     */
    public void setType(NotificationType type) {
        this.type = type;
    }

    /**
     * Setter for the timestamp of the notification
     * @param timestamp
     *      Timestamp to set for the notification
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Checks equality of this notification with another object
     * @param obj
     *      Object to compare with
     * @return
     *      True if obj is a Notification with the same ID, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Notification other = (Notification) obj;
        return id.equals(other.id);
    }



}
