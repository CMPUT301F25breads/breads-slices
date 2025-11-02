package com.example.slices.exceptions;

public class DBOpFailed extends RuntimeException {
    public DBOpFailed(String message) {
        super(message);
    }
}
