package com.example.slices.interfaces;

import com.example.slices.models.Image;

public interface ImageUploadCallback {
    void onSuccess(Image image);
    void onFailure(Exception e);
}
