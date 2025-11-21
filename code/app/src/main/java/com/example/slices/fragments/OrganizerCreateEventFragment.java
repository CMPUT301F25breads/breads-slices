package com.example.slices.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.slices.R;
import com.example.slices.controllers.QRCodeManager;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.util.Calendar;

/**
 * OrganizerCreateEventFragment.java
 *
 * Purpose: Fragment for creating a new event by an organizer.
 *          Handles user input for event details, date/time pickers,
 *          image upload, validation, and saving the event to the database.
 *
 * Outstanding Issues:
 * - Navigation back button is currently not implemented.
 * - QR code generation for the event once confirm button is pressed is TODO.
 * - Some event attributes that the organizer is asked to fill in are not part of the Event constructor yet so they are currently not used nor connected to the database.
 *
 * @author: Juliana
 *
 */

public class OrganizerCreateEventFragment extends Fragment {

    private EditText editEventName, editDescription, editGuidelines, editLocation;
    private EditText editDate, editTime, editRegStart, editRegEnd, editMaxWaiting, editMaxParticipants;
    private SwitchCompat switchEntrantLocation;
    private ImageView eventImage;
    private Uri imageUri;
    private Button buttonConfirm;
    private ImageButton uploadButton, backButton;

    /**
     * Launcher for picking an image from the gallery.
     */
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                        eventImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_create_event, container, false);
    }

    /**
     * Initializes UI components and sets up listeners for date/time pickers,
     * image upload, and event creation.
     *
     * @param view The root view.
     * @param savedInstanceState Bundle containing saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editEventName = view.findViewById(R.id.editEventName);
        editDescription = view.findViewById(R.id.editDescription);
        editGuidelines = view.findViewById(R.id.editGuidelines);
        editLocation = view.findViewById(R.id.editLocation);
        editDate = view.findViewById(R.id.editDate);
        editTime = view.findViewById(R.id.editTime);
        editRegStart = view.findViewById(R.id.editRegStart);
        editRegEnd = view.findViewById(R.id.editRegEnd);
        editMaxWaiting = view.findViewById(R.id.editMaxWaiting);
        editMaxParticipants = view.findViewById(R.id.editMaxParticipants);
        switchEntrantLocation = view.findViewById(R.id.switchEntrantLocation);
        eventImage = view.findViewById(R.id.eventImage);
        uploadButton = view.findViewById(R.id.uploadButton);
        backButton = view.findViewById(R.id.backButton);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);

        // TODO: Back button → navigate to organizer events (implement navigation)
//        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this)
//                .navigate(R.id.action_OrganizerCreateEventFragment_to_OrganizerEventsFragment));

        // Date picker
        editDate.setOnClickListener(v -> showDatePicker(editDate));
        editRegStart.setOnClickListener(v -> showDatePicker(editRegStart));
        editRegEnd.setOnClickListener(v -> showDatePicker(editRegEnd));

        // Time picker
        editTime.setOnClickListener(v -> showTimePicker(editTime));

        // Upload image
        uploadButton.setOnClickListener(v -> openGallery());

        // Confirm event creation
        buttonConfirm.setOnClickListener(v -> createEvent());
    }

    /**
     * Displays a DatePickerDialog for the given EditText.
     *
     * @param target EditText to populate with selected date.
     */
    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> target.setText((month + 1) + "/" + dayOfMonth + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    /**
     * Displays a TimePickerDialog for the given EditText.
     *
     * @param target EditText to populate with selected time.
     */
    private void showTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                    int hour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
                    target.setText(String.format("%02d:%02d %s", hour, minute, amPm));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePicker.show();
    }

    /**
     * Opens the gallery to select an event image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    /**
     * Collects input, validates it, creates an Event object,
     * and writes it to the database.
     */
    private void createEvent() {
        android.util.Log.d("CreateEvent", "createEvent() called - button clicked!");
        Toast.makeText(getContext(), "Creating event...", Toast.LENGTH_SHORT).show();

        String name = editEventName.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();
        String guide = editGuidelines.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String dateStr = editDate.getText().toString().trim();           // mm/dd/yyyy
        String timeStr = editTime.getText().toString().trim();           // hh:mm AM/PM
        String regStartStr = editRegStart.getText().toString().trim();  // mm/dd/yyyy
        String regEndStr = editRegEnd.getText().toString().trim();      // mm/dd/yyyy
        String maxWaitStr = editMaxWaiting.getText().toString().trim();
        String maxPartStr = editMaxParticipants.getText().toString().trim();
        boolean entrantLoc = switchEntrantLocation.isChecked();

        android.util.Log.d("CreateEvent", "Name: " + name + ", Desc: " + desc + ", Date: " + dateStr + ", Time: " + timeStr + ", Location: " + location);

        // Check required fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(dateStr) ||
                TextUtils.isEmpty(timeStr) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            android.util.Log.d("CreateEvent", "Validation failed - missing required fields");
            return;
        }

        try {
            // Parse event start datetime
            Calendar eventCal = Calendar.getInstance();
            String[] dateParts = dateStr.split("/");
            String[] timeParts = timeStr.split("[: ]");
            int month = Integer.parseInt(dateParts[0]) - 1;
            int day = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            String amPm = timeParts[2];
            if (amPm.equalsIgnoreCase("PM") && hour != 12) hour += 12;
            if (amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;
            eventCal.set(year, month, day, hour, minute, 0);
            Timestamp eventTimestamp = new Timestamp(eventCal.getTime());

            // Parse registration start/end dates
            Calendar regStartCal = Calendar.getInstance();
            Calendar regEndCal = Calendar.getInstance();
            if (!TextUtils.isEmpty(regStartStr)) {
                String[] regStartParts = regStartStr.split("/");
                regStartCal.set(Integer.parseInt(regStartParts[2]), Integer.parseInt(regStartParts[0]) - 1,
                        Integer.parseInt(regStartParts[1]), 0, 0, 0);
            }
            if (!TextUtils.isEmpty(regEndStr)) {
                String[] regEndParts = regEndStr.split("/");
                regEndCal.set(Integer.parseInt(regEndParts[2]), Integer.parseInt(regEndParts[0]) - 1,
                        Integer.parseInt(regEndParts[1]), 23, 59, 59);
            }
            Timestamp regStartTimestamp = new Timestamp(regStartCal.getTime());
            Timestamp regEndTimestamp = new Timestamp(regEndCal.getTime());

            // Max participants / waiting list
            int maxParticipants = TextUtils.isEmpty(maxPartStr) ? 0 : Integer.parseInt(maxPartStr);
            int maxWaiting = TextUtils.isEmpty(maxWaitStr) ? 0 : Integer.parseInt(maxWaitStr);

            // Organizer ID (if we want event to be associated to organizer)
            //String organizerID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Debug: Log the parsed timestamps
            android.util.Log.d("CreateEvent", "Event Date: " + eventTimestamp.toDate().toString());
            android.util.Log.d("CreateEvent", "Reg Deadline: " + regEndTimestamp.toDate().toString());
            android.util.Log.d("CreateEvent", "Current Time: " + new Timestamp(Calendar.getInstance().getTime()).toDate().toString());

            // Create event object - use testing constructor to bypass validation
            // The boolean flag bypasses date validation
            Event event = new Event(name, desc, location, eventTimestamp, regEndTimestamp, maxParticipants,
                    new EventCallback() {
                        @Override
                        public void onSuccess(Event createdEvent) {
                            android.util.Log.d("CreateEvent", "Event created with ID: " + createdEvent.getId());

                            // Generate QR code for the event
                            String qrCodeData = QRCodeManager.generateQRCodeData(createdEvent.getId());
                            Bitmap qrCodeBitmap = QRCodeManager.generateQRCode(createdEvent.getId());

                            if (qrCodeBitmap != null) {
                                Toast.makeText(getContext(), "Event created successfully with QR code!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Event created successfully! (QR code generation failed)", Toast.LENGTH_SHORT).show();
                            }

                            // Navigate back to organizer events to see the new event
                            NavHostFragment.findNavController(OrganizerCreateEventFragment.this)
                                    .navigateUp();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            android.util.Log.e("CreateEvent", "Failed to create event", e);
                            Toast.makeText(getContext(), "Failed to write event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates to the OrganizerEditEventFragment after event creation.
     *
     * @param bundle Bundle containing event ID.
     */
    private void navigateToEditFragment(Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(this);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build();

        navController.navigate(R.id.action_OrganizerEventsFragment_to_OrganizerEditEventFragment, bundle, options);
    }
}