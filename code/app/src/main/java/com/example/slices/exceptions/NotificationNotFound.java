package com.example.slices.exceptions;

public class NotificationNotFound extends RuntimeException {
    private String id;
    public NotificationNotFound(String message, String id) {
        super(message);
    }
    public String getId() {
        return id;
    }
}
