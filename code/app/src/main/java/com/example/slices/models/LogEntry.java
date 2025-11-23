package com.example.slices.models;

import com.google.firebase.Timestamp;

/**
 * Class representing a log entry
 * @author Ryan Haubrich
 * @version 1.0
 */
public abstract class LogEntry {
    /**
     * Message content of the log entry
     */
    protected String message;

    /**
     * Timestamp of when the log entry was created
     */
    protected Timestamp timestamp;

    /**
     * ID of the log entry
     */
    protected String logId;

    /**
     * Type of the log entry
     */
    protected LogType type;

    /**
     * Getter for the log ID
     * @return
     *      ID of the log entry
     */
    public String getLogId() {
        return logId;
    }

    /**
     * Getter for the message of the log entry
     * @return
     *      Message content of the log entry
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getter for the timestamp of the log entry
     * @return
     *      Timestamp of when the log entry was created
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the type of the log entry
     * @return
     *      Type of the log entry
     */
    public LogType getType() {
        return type;
    }

    /**
     * Setter for the message of the log entry
     * @param message
     *      Message content to set for the log entry
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Setter for the timestamp of the log entry
     * @param timestamp
     *      Timestamp to set for the log entry
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Setter for the log ID
     * @param logId
     *      ID to set for the log entry
     */
    public void setLogId(String logId) {
        this.logId = logId;
    }

    /**
     * Setter for the type of the log entry
     * @param type
     *      Type to set for the log entry
     */
    public void setType(LogType type) {
        this.type = type;
    }
}
