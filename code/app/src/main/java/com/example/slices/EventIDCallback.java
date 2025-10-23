package com.example.slices;

public interface EventIDCallback {
    void onSuccess(int id);
    void onFailure(Exception e);

}
