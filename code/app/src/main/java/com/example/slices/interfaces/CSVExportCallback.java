package com.example.slices.interfaces;

/**
 * Callback interface for CSV export operations
 * @author Kiro
 * @version 1.0
 */
public interface CSVExportCallback {
    /**
     * Called when CSV export succeeds
     * @param filePath Path where the CSV file was saved
     */
    void onSuccess(String filePath);

    /**
     * Called when CSV export fails
     * @param e Exception that caused the failure
     */
    void onFailure(Exception e);
}
