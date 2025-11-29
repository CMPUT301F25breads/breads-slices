package com.example.slices.interfaces;

public interface ImageUrlCallback {
    void onSuccess(String url);
    void onFailure(Exception e);
}
