package com.example.slices.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.slices.R;
import com.example.slices.databinding.DialogSendNotificationBinding;

/**
 * Helper class for creating notification composition dialogs
 */
public class NotificationDialog {

    // Interface for handling notification dialog results
    public interface NotificationDialogCallback {
        void onSendClicked(String title, String message);
        void onCancelClicked();
    }

    /**
     * Show a dialog for composing and sending notifications
     *
     * @param context Context for creating the dialog
     * @param callback Callback for handling user actions
     * @return AlertDialog instance
     */
    public static AlertDialog showNotificationDialog(Context context, NotificationDialogCallback callback) {
        LayoutInflater inflater = LayoutInflater.from(context);
        DialogSendNotificationBinding binding = DialogSendNotificationBinding.inflate(inflater);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setCancelable(true)
                .create();

        // Handle send button click
        binding.btnSend.setOnClickListener(v -> {
            String title = binding.etNotificationTitle.getText().toString().trim();
            String message = binding.etNotificationMessage.getText().toString().trim();

            // Validate input
            if (title.isEmpty()) {
                binding.tilNotificationTitle.setError(context.getString(R.string.notification_title_required));
                return;
            }

            if (message.isEmpty()) {
                binding.tilNotificationMessage.setError(context.getString(R.string.notification_message_required));
                return;
            }

            // Clear any previous errors
            binding.tilNotificationTitle.setError(null);
            binding.tilNotificationMessage.setError(null);

            dialog.dismiss();
            callback.onSendClicked(title, message);
        });

        // Handle cancel button click
        binding.btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            callback.onCancelClicked();
        });

        dialog.show();
        return dialog;
    }

    // Show success toast message
    public static void showSuccessToast(Context context, int successCount) {
        String message = context.getString(R.string.notification_sent_success, successCount);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    // Show failure toast message
    public static void showFailureToast(Context context) {
        Toast.makeText(context, R.string.notification_send_failed, Toast.LENGTH_SHORT).show();
    }
}