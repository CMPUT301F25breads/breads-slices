package com.example.slices;

public interface DBWriteCallback {
    void onSuccess();
    void onFailure(Exception e);
}
