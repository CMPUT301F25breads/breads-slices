package com.example.slices.exceptions;

public class EntrantNotFound extends RuntimeException {
    private String id;
    public EntrantNotFound(String message, String id) {
        super(message);
        this.id = id;

    }

}
