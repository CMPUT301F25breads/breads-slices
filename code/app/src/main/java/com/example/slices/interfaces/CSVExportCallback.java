package com.example.slices.interfaces;

import android.net.Uri;

/**
 * Callback interface for CSV export operations
 * @author Kiro
 * @version 1.0
 */
public interface CSVExportCallback {
    /**
     * Called when CSV export succeeds
     * @param fileUri URI of the exported CSV file
     */
    void onSuccess(Uri fileUri);

    /**
     * Called when CSV export fails
     * @param e Exception that caused the failure
     */
    void onFailure(Exception e);
}
