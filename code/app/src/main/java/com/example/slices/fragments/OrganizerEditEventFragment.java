package com.example.slices.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.slices.R;
import com.example.slices.controllers.DBConnector;
import com.example.slices.controllers.QRCodeManager;
import com.example.slices.models.Event;
import com.example.slices.interfaces.EventCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * OrganizerEditEventFragment.java
 *
 * Purpose: Fragment that allows an organizer to view and edit details of an existing event.
 *          Handles loading event data from the database, editing fields, showing dialogs,
 *          date/time pickers, and navigation to related fragments (e.g., waiting list).
 *
 * Outstanding Issues:
 * - Editing event image is not yet implemented.
 * - Location-based maximum distance logic is partly implemented.
 * - Some fields (guidelines, location) are placeholders and need proper binding after adding attributes to the Event constructor
 * - Back button functionality is not implemented yet.
 *
 * @author: Juliana
 */

public class OrganizerEditEventFragment extends Fragment {

    private EditText editEventName, editDate, editTime, editRegStart, editRegEnd,
            editMaxWaiting, editMaxParticipants, editMaxDistance;
    private TextView textDescription, textGuidelines, textLocation;
    private ImageView eventImage, qrCodeImageView;
    private Button buttonShareQRCode;
    private SwitchCompat switchEntrantLocation;
    private LinearLayout layoutMaxDistance;
    private DBConnector dbConnector;
    private String eventID; // event being edited
    private String qrCodeData; // QR code data for the event
    private Bitmap qrCodeBitmap; // QR code bitmap for sharing

    /**
     * Default constructor.
     */
    public OrganizerEditEventFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater  LayoutInflater object.
     * @param container Parent view group.
     * @param savedInstanceState Bundle containing saved state.
     * @return The inflated view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_edit_event_fragment, container, false);
    }

    /**
     * Initializes UI components, sets listeners for date/time pickers,
     * edit dialogs, and loads event data.
     *
     * @param view The root view.
     * @param savedInstanceState Bundle containing saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Initialize DB Connector ---
        dbConnector = new DBConnector();

        // --- Initialize Views ---
        editEventName = view.findViewById(R.id.editEventName);
        editDate = view.findViewById(R.id.editDate);
        editTime = view.findViewById(R.id.editTime);
        editRegStart = view.findViewById(R.id.editRegStart);
        editRegEnd = view.findViewById(R.id.editRegEnd);
        editMaxWaiting = view.findViewById(R.id.editMaxWaiting);
        editMaxParticipants = view.findViewById(R.id.editMaxParticipants);
        editMaxDistance = view.findViewById(R.id.editMaxDistance);
        textDescription = view.findViewById(R.id.textDescription);
        textGuidelines = view.findViewById(R.id.textGuidelines);
        textLocation = view.findViewById(R.id.textLocation);
        eventImage = view.findViewById(R.id.eventImage);
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        buttonShareQRCode = view.findViewById(R.id.buttonShareQRCode);
        switchEntrantLocation = view.findViewById(R.id.switchEntrantLocation);
        layoutMaxDistance = view.findViewById(R.id.layoutMaxDistance);
        Button buttonViewWaitingList = view.findViewById(R.id.buttonViewWaitingList);
        ImageButton buttonEditDescription = view.findViewById(R.id.buttonEditDescription);
        ImageButton buttonEditGuidelines = view.findViewById(R.id.buttonEditGuidelines);
        ImageButton buttonEditLocation = view.findViewById(R.id.buttonEditLocation);
        ImageButton buttonEditImage = view.findViewById(R.id.buttonEditImage);
        ImageButton backButton = view.findViewById(R.id.backButton);

        // --- Handle switch visibility logic for Maximum Distance ---
        switchEntrantLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutMaxDistance.setVisibility(View.VISIBLE);
            } else {
                layoutMaxDistance.setVisibility(View.GONE);
                editMaxDistance.setText(""); // clear if turned off
            }
        });
        layoutMaxDistance.setVisibility(View.GONE);

        // --- Load placeholder image with Glide ---
        Glide.with(this)
                .load(R.drawable.ic_image)
                .placeholder(R.drawable.ic_image)
                .into(eventImage);

        // --- Date/Time pickers ---
        editDate.setOnClickListener(v -> showDatePickerDialog(editDate));
        editTime.setOnClickListener(v -> showTimePickerDialog(editTime));
        editRegStart.setOnClickListener(v -> showDatePickerDialog(editRegStart));
        editRegEnd.setOnClickListener(v -> showDatePickerDialog(editRegEnd));

        // --- Edit buttons (dialogs) ---
        buttonEditDescription.setOnClickListener(v ->
                showEditDialog("Edit Description", textDescription)
        );

        buttonEditGuidelines.setOnClickListener(v ->
                showEditDialog("Edit Guidelines", textGuidelines)
        );

        buttonEditLocation.setOnClickListener(v ->
                showEditDialog("Edit Location", textLocation)
        );

        buttonViewWaitingList.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventName", editEventName.getText().toString()); // example data
            navigateToEntrantsFragment(bundle);
        });

        // --- Share QR Code button ---
        buttonShareQRCode.setOnClickListener(v -> shareQRCode());

        // --- Other buttons (functionality not implemented yet)---
        buttonEditImage.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Image clicked", Toast.LENGTH_SHORT).show()
        );

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // --- Load event data ---
        eventID = getArguments() != null ? getArguments().getString("eventID") : null;
        qrCodeData = getArguments() != null ? getArguments().getString("qrCodeData") : null;

        if (eventID != null) {
            loadEventData(eventID);
            loadQRCode();
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads event data from the database using DBConnector.
     *
     * @param eventID String ID of the event to load.
     */
    private void loadEventData(String eventID) {
        int id;
        try {
            id = Integer.parseInt(eventID);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid event ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        dbConnector.getEvent(id, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    Toast.makeText(getContext(), "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- Populate UI ---
                editEventName.setText(event.getName());
                textDescription.setText(event.getDescription());
                textGuidelines.setText("event guidelines");
                textLocation.setText(event.getLocation());
                editMaxParticipants.setText(String.valueOf(event.getMaxEntrants()));
                editMaxWaiting.setText("50");
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                editDate.setText(dateFormat.format(event.getEventDate().toDate()));
                editRegEnd.setText(dateFormat.format(event.getRegDeadline().toDate()));


                // To be implemented later
                //        switchEntrantLocation.setChecked(event.isLocationRequired());
                //        if (event.isLocationRequired()) {
                //            layoutMaxDistance.setVisibility(View.VISIBLE);
                //            editMaxDistance.setText(String.valueOf(event.getMaxDistance()));
                //        }

                Glide.with(requireContext())
                        .load(event.getImageUrl())
                        .placeholder(R.drawable.ic_image)
                        .into(eventImage);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to load event data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Helper for Date Picker ---
    /**
     * Displays a DatePickerDialog for the given EditText.
     *
     * @param targetEditText EditText to populate with the selected date.
     */
    private void showDatePickerDialog(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                    targetEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // --- Helper for Time Picker ---
    /**
     * Displays a TimePickerDialog for the given EditText.
     *
     * @param targetEditText EditText to populate with the selected time.
     */
    private void showTimePickerDialog(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int displayHour = (selectedHour > 12) ? selectedHour - 12 : selectedHour;
                    if (displayHour == 0) displayHour = 12;
                    String time = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
                    targetEditText.setText(time);
                },
                hour,
                minute,
                false
        );
        timePickerDialog.show();
    }

    // --- Helper for Edit Dialog ---
    /**
     * Shows an AlertDialog with an EditText to edit text fields (description, guidelines, location).
     *
     * @param title Text for the dialog title.
     * @param targetTextView TextView to update with the input from the dialog.
     */
    private void showEditDialog(String title, TextView targetTextView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(title);

        // Create container layout for padding
        LinearLayout container = new LinearLayout(requireContext());
        container.setPadding(50, 40, 50, 10);
        container.setOrientation(LinearLayout.VERTICAL);

        // Create the editable text box
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(targetTextView.getText().toString());
        input.setSelection(input.getText().length());
        input.setMinLines(3);
        input.setMaxLines(6);
        input.setBackgroundResource(android.R.drawable.edit_text);
        input.setPadding(25, 25, 25, 25);

        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("💾 Save", (dialog, which) -> {
            targetTextView.setText(input.getText().toString());
            Toast.makeText(getContext(), title + " updated!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("✖ Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Navigates to the EventEntrantsFragment for viewing the waiting list.
     *
     * @param bundle Bundle containing any relevant data for the navigation.
     */
    private void navigateToEntrantsFragment(Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(this);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false) // stay within the same tab
                .setLaunchSingleTop(true)           // prevent duplicates
                .setRestoreState(true)              // restore fragment state if needed
                .build();

        navController.navigate(R.id.action_OrganizerEditEventFragment_to_EventEntrantsFragment, bundle, options);
    }

    /**
     * Loads and displays the QR code for the event.
     * If qrCodeData is provided via arguments, it uses that.
     * Otherwise, it generates a new QR code from the event ID.
     */
    private void loadQRCode() {
        if (qrCodeData != null && !qrCodeData.isEmpty()) {
            // QR code data was passed from event creation
            int eventIdFromQR = QRCodeManager.decodeQRCode(qrCodeData);
            if (eventIdFromQR != -1) {
                displayQRCode(eventIdFromQR);
            } else {
                handleMissingQRCode();
            }
        } else if (eventID != null) {
            // Generate QR code from event ID
            try {
                int id = Integer.parseInt(eventID);
                displayQRCode(id);
            } catch (NumberFormatException e) {
                handleMissingQRCode();
            }
        } else {
            handleMissingQRCode();
        }
    }

    /**
     * Generates and displays the QR code bitmap for the given event ID.
     *
     * @param eventId The event ID to generate QR code for
     */
    private void displayQRCode(int eventId) {
        qrCodeBitmap = QRCodeManager.generateQRCode(eventId);

        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
            qrCodeImageView.setVisibility(View.VISIBLE);
            buttonShareQRCode.setEnabled(true);
        } else {
            handleMissingQRCode();
        }
    }

    /**
     * Handles the case where QR code data is missing or invalid.
     * Hides the QR code display and disables the share button.
     */
    private void handleMissingQRCode() {
        qrCodeImageView.setVisibility(View.GONE);
        buttonShareQRCode.setEnabled(false);
        buttonShareQRCode.setAlpha(0.5f);
        Toast.makeText(getContext(), "QR code not available", Toast.LENGTH_SHORT).show();
    }

    /**
     * Shares the QR code using Android ShareSheet.
     * Saves the QR code bitmap to a temporary file and creates a share intent.
     */
    private void shareQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(getContext(), "QR code not available to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Create a temporary file to store the QR code
            File cachePath = new File(requireContext().getCacheDir(), "images");
            cachePath.mkdirs();

            String eventName = editEventName.getText().toString();
            String sanitizedName = eventName.replaceAll("[^a-zA-Z0-9-_]", "_");
            File qrFile = new File(cachePath, "QR_" + sanitizedName + ".png");

            // Write the bitmap to the file
            FileOutputStream stream = new FileOutputStream(qrFile);
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Get the content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    qrFile
            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code: " + eventName);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to view event details for: " + eventName);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Show the share sheet
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to share QR code: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error sharing QR code", Toast.LENGTH_SHORT).show();
        }
    }
}