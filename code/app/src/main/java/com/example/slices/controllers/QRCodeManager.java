package com.example.slices.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.QREncoder;
import com.example.slices.testing.DebugLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Manager class for QR code generation and management operations
 * Provides static methods for generating, decoding, and saving QR codes
 *
 * @author Saahil
 */
public class QRCodeManager {

    private static final String QR_CODE_PREFIX = "EVENT:";
    private static final String TAG = "QRCodeManager";

    /**
     * Generates a QR code bitmap for an event
     * Uses the QREncoder class to create the QR code
     *
     * @param eventId Event ID to encode in the QR code
     * @return Bitmap of the generated QR code, or null if generation fails
     */
    public static Bitmap generateQRCode(int eventId) {
        try {
            String qrData = QR_CODE_PREFIX + eventId;
            DebugLogger.d(TAG, "Generating QR code for event: " + eventId);
            Bitmap qrBitmap = QREncoder.encode(qrData);

            if (qrBitmap == null) {
                DebugLogger.d(TAG, "QR code generation failed for event: " + eventId);
                return null;
            }

            DebugLogger.d(TAG, "QR code generated successfully for event: " + eventId);
            return qrBitmap;
        } catch (Exception e) {
            DebugLogger.d(TAG, "Exception during QR code generation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Decodes QR code data to extract the event ID
     * Validates that the QR code follows the expected format
     *
     * @param qrData Scanned QR code data string
     * @return Event ID extracted from the QR code, or -1 if invalid format
     */
    public static int decodeQRCode(String qrData) {
        try {
            if (qrData == null || !qrData.startsWith(QR_CODE_PREFIX)) {
                DebugLogger.d(TAG, "Invalid QR code format: " + qrData);
                return -1;
            }

            String eventIdStr = qrData.substring(QR_CODE_PREFIX.length());
            int eventId = Integer.parseInt(eventIdStr);

            DebugLogger.d(TAG, "Decoded event ID: " + eventId);
            return eventId;
        } catch (NumberFormatException e) {
            DebugLogger.d(TAG, "Failed to parse event ID from QR code: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            DebugLogger.d(TAG, "Exception during QR code decoding: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Saves a QR code bitmap to device storage
     * Handles both legacy (pre-Q) and modern Android storage approaches
     *
     * @param context Application context
     * @param bitmap QR code bitmap to save
     * @param eventName Event name to use in the filename
     * @param callback Callback for success/failure notification
     */
    public static void saveQRCode(Context context, Bitmap bitmap, String eventName,
                                  DBWriteCallback callback) {
        if (context == null || bitmap == null || eventName == null) {
            DebugLogger.d(TAG, "Invalid parameters for saveQRCode");
            if (callback != null) {
                callback.onFailure(new IllegalArgumentException("Invalid parameters"));
            }
            return;
        }

        try {
            // Sanitize event name for filename
            String sanitizedName = eventName.replaceAll("[^a-zA-Z0-9-_]", "_");
            String filename = "QR_" + sanitizedName + "_" + System.currentTimeMillis() + ".png";

            DebugLogger.d(TAG, "Saving QR code with filename: " + filename);

            // Use MediaStore for Android Q and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveQRCodeModern(context, bitmap, filename, callback);
            } else {
                saveQRCodeLegacy(context, bitmap, filename, callback);
            }
        } catch (Exception e) {
            DebugLogger.d(TAG, "Exception during QR code save: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Saves QR code using MediaStore API
     *
     * @param context Application context
     * @param bitmap QR code bitmap
     * @param filename Filename for the saved image
     * @param callback Callback for success/failure notification
     */
    private static void saveQRCodeModern(Context context, Bitmap bitmap, String filename,
                                         DBWriteCallback callback) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCodes");

            Uri uri = context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri == null) {
                DebugLogger.d(TAG, "Failed to create MediaStore entry");
                if (callback != null) {
                    callback.onFailure(new IOException("Failed to create MediaStore entry"));
                }
                return;
            }

            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                if (outputStream == null) {
                    DebugLogger.d(TAG, "Failed to open output stream");
                    if (callback != null) {
                        callback.onFailure(new IOException("Failed to open output stream"));
                    }
                    return;
                }

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                DebugLogger.d(TAG, "QR code saved successfully to: " + uri.toString());

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        } catch (IOException e) {
            DebugLogger.d(TAG, "IOException during modern save: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Saves QR code
     *
     * @param context Application context
     * @param bitmap QR code bitmap
     * @param filename Filename for the saved image
     * @param callback Callback for success/failure notification
     */
    private static void saveQRCodeLegacy(Context context, Bitmap bitmap, String filename,
                                         DBWriteCallback callback) {
        try {
            File picturesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File qrCodesDir = new File(picturesDir, "QRCodes");

            if (!qrCodesDir.exists()) {
                boolean created = qrCodesDir.mkdirs();
                if (!created) {
                    DebugLogger.d(TAG, "Failed to create QRCodes directory");
                    if (callback != null) {
                        callback.onFailure(new IOException("Failed to create directory"));
                    }
                    return;
                }
            }

            File imageFile = new File(qrCodesDir, filename);

            try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                DebugLogger.d(TAG, "QR code saved successfully to: " + imageFile.getAbsolutePath());

                // Notify media scanner about the new file
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
                context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        } catch (IOException e) {
            DebugLogger.d(TAG, "IOException during legacy save: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Generates the QR code data string for an event
     * This is the string that will be encoded in the QR code
     *
     * @param eventId Event ID
     * @return QR code data string in format "EVENT:{eventId}"
     */
    public static String generateQRCodeData(int eventId) {
        return QR_CODE_PREFIX + eventId;
    }
}
