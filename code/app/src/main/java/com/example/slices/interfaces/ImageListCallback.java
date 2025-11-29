package com.example.slices.interfaces;

import com.example.slices.models.Image;

import java.util.List;

public interface ImageListCallback {
    void onSuccess(List<Image> imageList);
    void onFailure(Exception e);
}
