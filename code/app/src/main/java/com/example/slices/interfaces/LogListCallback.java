package com.example.slices.interfaces;

import com.example.slices.models.LogEntry;

import java.util.List;

public interface LogListCallback {
    void onSuccess(List<LogEntry> logs);
    void onFailure(Exception e);

}
