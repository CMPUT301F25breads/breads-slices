package com.example.slices.exceptions;

public class WaitlistFull extends RuntimeException {
    public WaitlistFull(String message) {
        super(message);
    }
}
