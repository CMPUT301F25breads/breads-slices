package com.example.slices.models;

import com.google.firebase.Timestamp;


public class Notification {
    protected String title;
    protected String body;
    protected int notificationId;
    protected int recipientId;

    protected int senderId;

    protected boolean read;

    protected Timestamp timestamp;
    
    protected NotificationType type;
    protected int eventId;




    //Default constructor for Firebase
    public Notification() {
    }

    public Notification(String title, String body, int notificationId, int recipientId, int senderId) {
        this.title = title;
        this.body = body;
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.read = false;
        this.timestamp = Timestamp.now();
        this.type = NotificationType.NOTIFICATION;
    }



    public String getTitle() {
        return title;
    }



    public String getBody() {
        return body;
    }

    public int getId() {
        return notificationId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public int getSenderId() {
        return senderId;
    }



    public Timestamp getTimestamp() {
        return timestamp;
    }
    public NotificationType getType() {
        return type;
    }
    public int getEventId() {
        return eventId;
    }

    public boolean getRead() {
        return read;
    }

    public void setId (int id) {
        this.notificationId = id;
    }



    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }



    public void setRead(boolean read) {
        this.read = read;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public void setType(NotificationType type) {
        this.type = type;
    }


    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Notification other = (Notification) obj;
        return notificationId == other.notificationId;
    }

    @Override
    public int hashCode() {
        return notificationId;
    }


}
