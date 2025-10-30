package com.example.slices.interfaces;

import com.example.slices.models.Log;

import java.util.List;

public interface LogListCallback {
    void onSuccess(List<Log> logs);
    void onFailure(Exception e);

}
