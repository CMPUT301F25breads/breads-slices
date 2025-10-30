package com.example.slices.models;

import com.google.firebase.Timestamp;

public abstract class Log {
    protected String message;
    protected Timestamp timestamp;
    protected int logId;
    protected LogType type;




    public int getLogId() {
        return logId;
    }
}
