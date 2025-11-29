package com.example.slices.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.example.slices.controllers.EventController;
import com.example.slices.controllers.ImageController;
import com.example.slices.controllers.QRCodeManager;
import com.example.slices.models.Event;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.EventInfo;
import com.google.firebase.Timestamp;

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

    private static final int PICK_IMAGE_REQUEST = 1001;

    private EditText editEventName, editDate, editTime, editRegStart, editRegEnd,
            editMaxWaiting, editMaxParticipants, editMaxDistance;
    private TextView textDescription, textGuidelines, textLocation, textEventTitle;
    private ImageView eventImage, qrCodeImageView;
    private Button buttonShareQRCode;
    private SwitchCompat switchEntrantLocation;
    private LinearLayout layoutMaxDistance;

    private String eventID; // event being edited
    private String qrCodeData; // QR code data for the event
    private Bitmap qrCodeBitmap; // QR code bitmap for sharing
    private Event currentEvent; // The event being edited
    private Uri selectedImageUri; // URI of the selected image

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
        textEventTitle = view.findViewById(R.id.textEventTitle);
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
            // Only save if user is interacting (not during initial load)
            // buttonView.isPressed() returns true only when user actually clicks
            if (buttonView.isPressed()) {
                saveEntrantLocationSettings();
            }
        });
        layoutMaxDistance.setVisibility(View.GONE);

        // Add listener for max distance field
        editMaxDistance.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveEntrantLocationSettings();
            }
        });

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

        // --- Add listeners for direct edit fields ---
        editEventName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveEventName();
            }
        });

        editMaxParticipants.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveMaxParticipants();
            }
        });

        editMaxWaiting.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveMaxWaitingCapacity();
            }
        });

        buttonViewWaitingList.setOnClickListener(v -> {
            if (currentEvent == null || eventID == null) {
                Toast.makeText(getContext(), "Event data not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Bundle bundle = new Bundle();
            bundle.putInt("event_id", currentEvent.getId());
            bundle.putString("event_name", currentEvent.getEventInfo().getName());
            bundle.putInt("sender_id", currentEvent.getEventInfo().getOrganizerID());
            navigateToEntrantsFragment(bundle);
        });

        // --- Share QR Code button ---
        buttonShareQRCode.setOnClickListener(v -> shareQRCode());

        // --- Edit Image button - opens gallery ---
        buttonEditImage.setOnClickListener(v -> openImagePicker());

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

        EventController.getEvent(id, new EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (event == null) {
                    Toast.makeText(getContext(), "Event not found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Store the event object for later updates
                currentEvent = event;
                EventInfo eventInfo = event.getEventInfo();


                // --- Populate UI ---
                editEventName.setText(eventInfo.getName());
                textEventTitle.setText(eventInfo.getName());
                textDescription.setText(eventInfo.getDescription());
                textGuidelines.setText(eventInfo.getGuidelines());
                textLocation.setText(eventInfo.getAddress());
                editMaxParticipants.setText(String.valueOf(eventInfo.getMaxEntrants()));

                // Display waiting list capacity (show empty if unlimited/default)
                int waitlistCapacity = event.getWaitlist().getMaxCapacity();
                if (waitlistCapacity > 32768) {
                    editMaxWaiting.setText(""); // Unlimited
                } else {
                    editMaxWaiting.setText(String.valueOf(waitlistCapacity));
                }

                // Format and display date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                editDate.setText(dateFormat.format(eventInfo.getEventDate().toDate()));
                editRegEnd.setText(dateFormat.format(eventInfo.getRegEnd().toDate()));

                // Format and display time
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                editTime.setText(timeFormat.format(eventInfo.getEventDate().toDate()));

                // Load entrant location settings
                boolean requiresLocation = eventInfo.getEntrantLoc();
                switchEntrantLocation.setChecked(requiresLocation);
                if (requiresLocation) {
                    layoutMaxDistance.setVisibility(View.VISIBLE);
                    String distMetersStr = eventInfo.getEntrantDist();
                    if (distMetersStr != null && !distMetersStr.isEmpty()) {
                        try {
                            // Convert meters to kilometers for display
                            int distanceMeters = Integer.parseInt(distMetersStr);
                            int distanceKm = distanceMeters / 1000;
                            // Only show the value if it's greater than 0
                            if (distanceKm > 0) {
                                editMaxDistance.setText(String.valueOf(distanceKm));
                            } else {
                                editMaxDistance.setText("500"); // Default if 0
                            }
                        } catch (NumberFormatException e) {
                            // If parsing fails, show default
                            editMaxDistance.setText("500");
                        }
                    } else {
                        // No distance set, show default
                        editMaxDistance.setText("500");
                    }
                } else {
                    layoutMaxDistance.setVisibility(View.GONE);
                }

                Glide.with(requireContext())
                        .load(eventInfo.getImage().getUrl())
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

                    // Save to database based on which field was edited
                    if (targetEditText == editDate) {
                        saveEventDateTime();
                    } else if (targetEditText == editRegEnd) {
                        saveRegistrationDeadline();
                    }
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

                    // Save event date/time when time is changed
                    if (targetEditText == editTime) {
                        saveEventDateTime();
                    }
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
            String newValue = input.getText().toString();

            // Update the TextView
            targetTextView.setText(newValue);

            // First get the existing event info
            EventInfo currentEventInfo = currentEvent.getEventInfo();

            // ------------------------------------------
            // I added logic for all fields but I dont know what the UI looks like
            // -Ryan
            // ------------------------------------------
            // Update the EventInfo object based on which field was edited
            if (title.equals("Edit Description")) {
                currentEventInfo.setDescription(newValue);
            } else if (title.equals("Edit Guidelines")) {
                currentEventInfo.setGuidelines(newValue);
            } else if (title.equals("Edit Image")) {
                currentEventInfo.setImageUrl(newValue);
            } else if (title.equals("Edit Name")) {
                currentEventInfo.setName(newValue);
            } else if (title.equals("Edit Max Participants")) {
                currentEventInfo.setMaxEntrants(Integer.parseInt(newValue));
            }




            // Save to database
            EventController.updateEventInfo(currentEvent, currentEventInfo, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), title + " saved to database!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
     * Saves the event date and time to the database
     * Combines the date from editDate and time from editTime fields
     */
    private void saveEventDateTime() {
        if (currentEvent == null) return;

        String dateStr = editDate.getText().toString().trim();
        String timeStr = editTime.getText().toString().trim();

        if (dateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse the date (format: MM/dd/yyyy)
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            int month = Integer.parseInt(dateParts[0]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            // Parse time if available (format: hh:mm AM/PM)
            if (!timeStr.isEmpty()) {
                String[] timeParts = timeStr.split(":");
                if (timeParts.length == 2) {
                    int hour = Integer.parseInt(timeParts[0]);
                    String[] minuteAndAmPm = timeParts[1].split(" ");
                    int minute = Integer.parseInt(minuteAndAmPm[0]);

                    if (minuteAndAmPm.length > 1) {
                        String amPm = minuteAndAmPm[1];
                        if (amPm.equals("PM") && hour != 12) {
                            hour += 12;
                        } else if (amPm.equals("AM") && hour == 12) {
                            hour = 0;
                        }
                    }

                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                }
            }

            Timestamp newEventDate = new Timestamp(calendar.getTime());
            currentEvent.getEventInfo().setEventDate(newEventDate);

            EventController.updateEvent(currentEvent, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Event date/time saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save date/time: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error parsing date/time: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the registration deadline to the database
     */
    private void saveRegistrationDeadline() {
        if (currentEvent == null) return;

        String dateStr = editRegEnd.getText().toString().trim();

        if (dateStr.isEmpty()) {
            Toast.makeText(getContext(), "Please select a registration deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Parse the date (format: MM/dd/yyyy)
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            int month = Integer.parseInt(dateParts[0]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, 23, 59, 59); // Set to end of day

            Timestamp newRegDeadline = new Timestamp(calendar.getTime());

            currentEvent.getEventInfo().setRegEnd(newRegDeadline);

            EventController.updateEvent(currentEvent, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Registration deadline saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save deadline: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error parsing date: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the event name to the database
     */
    private void saveEventName() {
        if (currentEvent == null) return;

        String newName = editEventName.getText().toString().trim();
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Event name cannot be empty", Toast.LENGTH_SHORT).show();
            editEventName.setText(currentEvent.getEventInfo().getName());
            return;
        }

        EventInfo currentEventInfo = currentEvent.getEventInfo();
        currentEventInfo.setName(newName);
        EventController.updateEventInfo(currentEvent, currentEventInfo, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                textEventTitle.setText(newName);
                Toast.makeText(getContext(), "Event name saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to save event name: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Saves the waiting list capacity to the database
     */
    private void saveMaxWaitingCapacity() {
        if (currentEvent == null) return;

        String maxWaitingStr = editMaxWaiting.getText().toString().trim();
        if (maxWaitingStr.isEmpty()) {
            // If empty, set to unlimited (default value)
            currentEvent.getWaitlist().setMaxCapacity(32768);
            EventController.updateEvent(currentEvent, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Waiting list capacity set to unlimited", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save waiting list capacity: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        try {
            int maxWaiting = Integer.parseInt(maxWaitingStr);
            if (maxWaiting <= 0) {
                Toast.makeText(getContext(), "Waiting list capacity must be greater than 0",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            currentEvent.getWaitlist().setMaxCapacity(maxWaiting);
            EventController.updateEvent(currentEvent, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Waiting list capacity saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save waiting list capacity: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the max participants to the database
     */
    private void saveMaxParticipants() {
        if (currentEvent == null) return;

        String maxParticipantsStr = editMaxParticipants.getText().toString().trim();
        if (maxParticipantsStr.isEmpty()) {
            return; // Don't save if empty
        }

        try {
            int maxParticipants = Integer.parseInt(maxParticipantsStr);
            if (maxParticipants <= 0) {
                Toast.makeText(getContext(), "Max participants must be greater than 0",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            currentEvent.getEventInfo().setMaxEntrants(maxParticipants);
            EventController.updateEvent(currentEvent, new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Max participants saved!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to save max participants: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Shares the QR code using Android ShareSheet.
     * Creates a temporary file for the QR code and opens the system share dialog.
     */
    private void shareQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(getContext(), "QR code not available to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Step 1: Save QR code to a temporary file
            File qrFile = saveQRCodeToTempFile();

            // Step 2: Get a shareable URI for the file
            Uri shareableUri = getShareableUri(qrFile);

            // Step 3: Create and launch the share intent
            launchShareIntent(shareableUri);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to share QR code: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error sharing QR code", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the QR code bitmap to a temporary file in the app's cache directory.
     * The file is named using the event name to make it identifiable.
     *
     * @return The temporary file containing the QR code image
     * @throws IOException if file creation or writing fails
     */
    private File saveQRCodeToTempFile() throws IOException {
        // Create a folder in the cache directory for temporary images
        File imageFolder = new File(requireContext().getCacheDir(), "images");
        imageFolder.mkdirs(); // Create the folder if it doesn't exist

        // Get event name and make it safe for use in a filename
        String eventName = editEventName.getText().toString();

        // Create the file
        File qrFile = new File(imageFolder, "QR_" + eventName + ".png");

        // Write the QR code bitmap to the file
        try (FileOutputStream stream = new FileOutputStream(qrFile)) {
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }

        return qrFile;
    }

    /**
     * Gets a shareable URI for a file using FileProvider.
     *
     * @param file The file to get a URI for
     * @return A content:// URI that other apps can access
     */
    private Uri getShareableUri(File file) {
        return FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file
        );
    }

    /**
     * Creates and launches a share intent to share the QR code with other apps.
     *
     * @param imageUri The URI of the QR code image to share
     */
    private void launchShareIntent(Uri imageUri) {
        String eventName = editEventName.getText().toString();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Event QR Code: " + eventName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to view event details for: " + eventName);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Show Android's share dialog
        startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
    }

    /**
     * Saves the entrant location settings (toggle and max distance) to the database
     * Distance is stored in meters but displayed in kilometers
     * When enabling geolocation, captures the event location coordinates
     */
    private void saveEntrantLocationSettings() {
        if (currentEvent == null) return;

        boolean requiresLocation = switchEntrantLocation.isChecked();
        String maxDistKmStr = editMaxDistance.getText().toString().trim();

        // Store in meters in database
        String entrantDistMeters;
        
        if (requiresLocation) {
            // Location is required - validate and convert the distance
            if (maxDistKmStr.isEmpty()) {
                // If field is empty but location is required, use default
                entrantDistMeters = "500000"; // Default: 500km in meters
            } else {
                try {
                    int distanceKm = Integer.parseInt(maxDistKmStr);
                    // Validate range: 1km to 500km
                    if (distanceKm < 1) {
                        Toast.makeText(getContext(), "Distance must be at least 1 km", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (distanceKm > 500) {
                        Toast.makeText(getContext(), "Distance cannot exceed 500 km", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Convert km to meters for storage
                    entrantDistMeters = String.valueOf(distanceKm * 1000);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid distance format", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Check if event already has coordinates
            EventInfo currentEventInfo = currentEvent.getEventInfo();
            if (currentEventInfo.getEventLatitude() == null || currentEventInfo.getEventLongitude() == null) {
                // Event doesn't have coordinates yet - need to capture location
                android.util.Log.d("OrganizerEditEvent", "Geolocation enabled - capturing event location");
                
                // Check if we have location permission
                if (com.example.slices.controllers.LocationManager.hasLocationPermission(requireContext())) {
                    com.example.slices.controllers.LocationManager locationManager = 
                        new com.example.slices.controllers.LocationManager();
                    
                    locationManager.getUserLocation(requireContext(), new com.example.slices.interfaces.LocationCallback() {
                        @Override
                        public void onSuccess(android.location.Location eventLocation) {
                            android.util.Log.d("OrganizerEditEvent", "Got location for event: lat=" + 
                                             eventLocation.getLatitude() + ", lon=" + eventLocation.getLongitude());
                            
                            // Set the location on EventInfo
                            currentEventInfo.setLocation(eventLocation);
                            currentEventInfo.setEntrantLoc(requiresLocation);
                            currentEventInfo.setEntrantDist(entrantDistMeters);
                            
                            // Save to database
                            saveEventInfoToDatabase(currentEventInfo);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            android.util.Log.w("OrganizerEditEvent", "Failed to get location", e);
                            Toast.makeText(getContext(), 
                                "Warning: Unable to get event location. Distance validation won't work until location is set.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Still save the settings, just without coordinates
                            currentEventInfo.setEntrantLoc(requiresLocation);
                            currentEventInfo.setEntrantDist(entrantDistMeters);
                            saveEventInfoToDatabase(currentEventInfo);
                        }
                    });
                    return; // Exit early - callback will handle saving
                } else {
                    // No location permission
                    android.util.Log.w("OrganizerEditEvent", "No location permission for geolocation event");
                    Toast.makeText(getContext(), 
                        "Warning: Grant location permission for distance validation to work.", 
                        Toast.LENGTH_LONG).show();
                }
            }
        } else {
            // Location not required - keep existing value or use default
            EventInfo currentEventInfo = currentEvent.getEventInfo();
            String existingDist = currentEventInfo.getEntrantDist();
            entrantDistMeters = (existingDist != null && !existingDist.isEmpty()) ? existingDist : "500000";
        }

        // Save settings (either location not required, or already has coordinates, or no permission)
        EventInfo currentEventInfo = currentEvent.getEventInfo();
        currentEventInfo.setEntrantLoc(requiresLocation);
        currentEventInfo.setEntrantDist(entrantDistMeters);
        saveEventInfoToDatabase(currentEventInfo);
    }
    
    /**
     * Helper method to save EventInfo to database
     * Extracted to avoid code duplication
     */
    private void saveEventInfoToDatabase(EventInfo eventInfo) {
        EventController.updateEventInfo(currentEvent, eventInfo, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                // Only show toast if fragment is still attached
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Location settings saved!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Only show toast if fragment is still attached
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Failed to save location settings: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Opens the device's image picker to select a new event poster
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result from the image picker
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == android.app.Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            
            if (selectedImageUri != null) {
                // Modify existing file at the path
                ImageController.modifyImage(currentEvent.getEventInfo().getImage().getPath(), selectedImageUri, new DBWriteCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "New image saved!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(getContext(), "Failed to update image: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });


                // Display the selected image immediately
                Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.ic_image)
                        .into(eventImage);

                // Save the image URL to the database
                //saveEventImage(selectedImageUri.toString());
            }
        }
    }

    /**
     * Saves the new event image URL to the database
     * @param imageUrl The URL/URI of the new image
     */
    private void saveEventImage(String imageUrl) {
        if (currentEvent == null) return;

        EventInfo currentEventInfo = currentEvent.getEventInfo();
        currentEventInfo.setImageUrl(imageUrl);

        EventController.updateEventInfo(currentEvent, currentEventInfo, new DBWriteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Event poster updated!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to update poster: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                // Revert to old image on failure
                if (currentEvent.getEventInfo().getImageUrl() != null) {
                    Glide.with(requireContext())
                            .load(currentEvent.getEventInfo().getImageUrl())
                            .placeholder(R.drawable.ic_image)
                            .into(eventImage);
                }
            }
        });
    }

    /**
     * Called when the fragment is paused (user navigates away).
     * Saves any pending changes to ensure data isn't lost.
     */
    @Override
    public void onPause() {
        super.onPause();
        
        // Save any pending changes before the fragment is paused
        if (currentEvent != null) {
            // Save location settings if the distance field has content
            if (switchEntrantLocation.isChecked() && !editMaxDistance.getText().toString().trim().isEmpty()) {
                saveEntrantLocationSettings();
            }
            
            // Save other fields that might have changed
            String currentName = editEventName.getText().toString().trim();
            if (!currentName.isEmpty() && !currentName.equals(currentEvent.getEventInfo().getName())) {
                saveEventName();
            }
            
            String maxParticipantsStr = editMaxParticipants.getText().toString().trim();
            if (!maxParticipantsStr.isEmpty()) {
                try {
                    int maxParticipants = Integer.parseInt(maxParticipantsStr);
                    if (maxParticipants != currentEvent.getEventInfo().getMaxEntrants()) {
                        saveMaxParticipants();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
            
            String maxWaitingStr = editMaxWaiting.getText().toString().trim();
            if (!maxWaitingStr.isEmpty()) {
                try {
                    int maxWaiting = Integer.parseInt(maxWaitingStr);
                    if (maxWaiting != currentEvent.getWaitlist().getMaxCapacity()) {
                        saveMaxWaitingCapacity();
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        }
    }
}