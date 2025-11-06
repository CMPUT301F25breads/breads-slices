package com.example.slices.interfaces;

import com.example.slices.models.LogEntry;

import java.util.List;

/**
 * Interface for log list callbacks
 * @author Ryan Haubrich
 * @version 1.0
 */
public interface LogListCallback {
    void onSuccess(List<LogEntry> logs);
    void onFailure(Exception e);

}
