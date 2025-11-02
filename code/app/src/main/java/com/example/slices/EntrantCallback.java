package com.example.slices;

public interface EntrantCallback {
    void onSuccess(Entrant entrant);
    void onFailure(Exception e);
}
