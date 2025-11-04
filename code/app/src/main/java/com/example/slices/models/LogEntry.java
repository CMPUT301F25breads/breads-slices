package com.example.slices.models;

import com.google.firebase.Timestamp;

public abstract class LogEntry {
    protected String message;
    protected Timestamp timestamp;
    protected int logId;
    protected LogType type;






    public int getLogId() {
        return logId;
    }
    public String getMessage() {
        return message;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public LogType getType() {
        return type;
    }


}
