package com.example.slices;

public class DBOpFailed extends RuntimeException {
    public DBOpFailed(String message) {
        super(message);
    }
}
