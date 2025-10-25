package com.example.slices;

public class EventNotFound extends RuntimeException {
    private String id;
    public EventNotFound(String message, String id) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
