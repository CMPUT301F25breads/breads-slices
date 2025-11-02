package com.example.slices.interfaces;

public interface DBWriteCallback {
    void onSuccess();
    void onFailure(Exception e);
}
