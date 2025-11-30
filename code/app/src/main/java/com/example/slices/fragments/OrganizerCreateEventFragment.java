package com.example.slices.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.controllers.EventController;
import com.example.slices.controllers.ImageController;
import com.example.slices.controllers.QRCodeManager;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.interfaces.EventIDCallback;
import com.example.slices.interfaces.ImageUploadCallback;
import com.example.slices.models.Event;
import com.example.slices.models.EventInfo;
import com.example.slices.models.Image;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;

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
    private SharedViewModel svm;
    private EditText editDate, editTime, editRegStart, editRegEnd, editMaxWaiting, editMaxParticipants, editMaxDistance;
    private SwitchCompat switchEntrantLocation;
    private LinearLayout layoutMaxDistance;
    private ImageView eventImage;
    private Uri imageUri;
    private Button buttonConfirm;
    private ImageButton uploadButton, backButton;
    private Image image;
    //private String entrantDist;

    /**
     * Stores event data while waiting for location permission response
     */
    private EventInfo pendingEventInfo;

    /**
     * Launcher for requesting location permissions
     */
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

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
                        ImageController.uploadImage(imageUri, String.valueOf(svm.getUser().getId()),
                                new ImageUploadCallback() {

                                    @Override
                                    public void onSuccess(Image newImage) {
                                        Log.d("IMG", "URL = ");
                                        image = newImage;
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("IMG", "Upload failed", e);
                                    }
                                }
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    /**
     * Sets up the location permission launcher to handle permission request results.
     * Must be called in onViewCreated() before the fragment is fully created.
     */
    private void setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                // Check if any of the location permissions were granted
                boolean fineLocationGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseLocationGranted = Boolean.TRUE.equals(permissions.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                
                if (fineLocationGranted || coarseLocationGranted) {
                    // Permission granted - proceed with event creation
                    if (pendingEventInfo != null) {
                        createEventWithLocation(pendingEventInfo);
                    } else {
                        // Safety check: should not happen, but clear state if it does
                        Toast.makeText(getContext(), 
                            "Error: Event information was lost. Please try again.", 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Permission denied - show clear error message and do not create event
                    Toast.makeText(getContext(), 
                        "Location permission is required to create geolocation-enabled events", 
                        Toast.LENGTH_LONG).show();
                    // Clear pending event info to prevent stale data
                    pendingEventInfo = null;
                }
            }
        );
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

        svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Setup location permission launcher
        setupLocationPermissionLauncher();

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
        editMaxDistance = view.findViewById(R.id.editMaxDistance);
        switchEntrantLocation = view.findViewById(R.id.switchEntrantLocation);
        layoutMaxDistance = view.findViewById(R.id.layoutMaxDistance);
        eventImage = view.findViewById(R.id.eventImage);
        uploadButton = view.findViewById(R.id.uploadButton);
        backButton = view.findViewById(R.id.backButton);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);

        // Handle switch visibility logic for Maximum Distance
        switchEntrantLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                layoutMaxDistance.setVisibility(View.VISIBLE);
            } else {
                layoutMaxDistance.setVisibility(View.GONE);
                editMaxDistance.setText(""); // clear if turned off
            }
        });

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
        // Disable button to prevent double-clicks
        buttonConfirm.setEnabled(false);
        Toast.makeText(getContext(), "Creating event...", Toast.LENGTH_SHORT).show();

        String name = editEventName.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();
        String guide = editGuidelines.getText().toString().trim();
        //Change from Ryan
        String address = editLocation.getText().toString().trim();
        Location location = null;
        String dateStr = editDate.getText().toString().trim();           // mm/dd/yyyy
        String timeStr = editTime.getText().toString().trim();           // hh:mm AM/PM
        String regStartStr = editRegStart.getText().toString().trim();  // mm/dd/yyyy
        String regEndStr = editRegEnd.getText().toString().trim();      // mm/dd/yyyy
        String maxWaitStr = editMaxWaiting.getText().toString().trim();
        String maxPartStr = editMaxParticipants.getText().toString().trim();
        boolean entrantLoc = switchEntrantLocation.isChecked();

        // Check required fields
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(dateStr) ||
                TextUtils.isEmpty(timeStr)) {
            Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            buttonConfirm.setEnabled(true);
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

            // Max participants / waiting list - default to 100 if not specified
            int maxParticipants = TextUtils.isEmpty(maxPartStr) ? 100 : Integer.parseInt(maxPartStr);

            // Just changed this because I kept creating events with a max waitlist of 0 - Brad
            int maxWaiting = TextUtils.isEmpty(maxWaitStr) ? Integer.MAX_VALUE : Integer.parseInt(maxWaitStr);

            // Organizer ID (if we want event to be associated to organizer)
            int organizerID = svm.getUser().getId();



            if(image != null) {
                buildEvent(name, desc, address, guide, image.getUrl(), eventTimestamp, regStartTimestamp, regEndTimestamp,
                        maxParticipants, maxWaiting, entrantLoc, 0, organizerID, image);
            }
            else {
                buildEvent(name, desc, address, guide, null, eventTimestamp, regStartTimestamp, regEndTimestamp,
                        maxParticipants, maxWaiting, entrantLoc, 0, organizerID, new Image());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating event: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Clear any pending event info on error
            pendingEventInfo = null;
            // Re-enable button so user can try again
            buttonConfirm.setEnabled(true);
        }
    }

    private void buildEvent(String name, String desc, String address, String guide,
                            String imgUrl, Timestamp eventTimestamp, Timestamp regStartTimestamp, Timestamp regEndTimestamp,
                            int maxParticipants, int maxWaiting, boolean entrantLoc, int id, int organizerID, Image image) {

        // Get entrant distance if location is required
        // Store in meters in database, but UI shows kilometers
        String entrantDist = "500000"; // Default: 500km in meters
        if (entrantLoc) {
            String maxDistKmStr = editMaxDistance.getText().toString().trim();
            if (!TextUtils.isEmpty(maxDistKmStr)) {
                try {
                    int distanceKm = Integer.parseInt(maxDistKmStr);
                    // Validate range: 1km to 500km
                    if (distanceKm < 1) {
                        Toast.makeText(getContext(), "Distance must be at least 1 km", Toast.LENGTH_SHORT).show();
                        buttonConfirm.setEnabled(true);
                        return;
                    }
                    if (distanceKm > 500) {
                        Toast.makeText(getContext(), "Distance cannot exceed 500 km", Toast.LENGTH_SHORT).show();
                        buttonConfirm.setEnabled(true);
                        return;
                    }
                    // Convert km to meters for storage
                    entrantDist = String.valueOf(distanceKm * 1000);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid distance format", Toast.LENGTH_SHORT).show();
                    buttonConfirm.setEnabled(true);
                    return;
                }
            }
        }

        EventInfo eventInfo = new EventInfo(name, desc, address, guide, null, eventTimestamp, regStartTimestamp, regEndTimestamp,
                maxParticipants, maxWaiting, entrantLoc, entrantDist, 0, organizerID, image);

        // Check if geolocation is required
        if (entrantLoc) {
            // For geolocation events: request permission and get location
            requestLocationPermissionForEvent(eventInfo);
        } else {
            // For non-geolocation events: create immediately
            createEventWithInfo(eventInfo);
        }

    }

    /**
     * Helper method to create event with the given EventInfo
     * Extracted to avoid code duplication when handling location async
     */
    private void createEventWithInfo(EventInfo eventInfo) {
        EventController.createEvent(eventInfo, new EventCallback() {
            @Override
            public void onSuccess(Event createdEvent) {
                // Generate QR code for the event
                String qrCodeData = QRCodeManager.generateQRCodeData(createdEvent.getId());
                Bitmap qrCodeBitmap = QRCodeManager.generateQRCode(createdEvent.getId());

                if (qrCodeBitmap != null) {
                    Toast.makeText(getContext(), "Event created successfully with QR code!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Event created successfully! (QR code generation failed)", Toast.LENGTH_SHORT).show();
                }

                // Clear pending event info on success
                pendingEventInfo = null;

                // Navigate to organizer events list to see the new event
                navigateToOrgEventsFragment();
//                NavHostFragment.findNavController(OrganizerCreateEventFragment.this)
//                        .navigate(R.id.action_to_organizerEventsFragment);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                // Clear pending event info on failure
                pendingEventInfo = null;
                // On failure, stay on the create event screen (no navigation)
            }
        });
    }

    /**
     * Requests location permission for creating a geolocation-enabled event.
     * If permissions are already granted, immediately proceeds to create the event with location.
     * If not granted, launches the permission request dialog.
     * 
     * @param eventInfo The event information to create
     */
    private void requestLocationPermissionForEvent(EventInfo eventInfo) {
        // Store the event info while we wait for permission response
        this.pendingEventInfo = eventInfo;
        
        // Check if permissions are already granted
        if (com.example.slices.controllers.LocationManager.hasLocationPermission(requireContext())) {
            // Permissions already granted - proceed immediately
            createEventWithLocation(eventInfo);
        } else {
            // Need to request permissions
            locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Gets the organizer's location and creates the event with those coordinates.
     * Called after location permissions are granted.
     * 
     * @param eventInfo The event information to create
     */
    private void createEventWithLocation(EventInfo eventInfo) {
        com.example.slices.controllers.LocationManager locationManager = 
            new com.example.slices.controllers.LocationManager();
        
        locationManager.getUserLocation(requireContext(), new com.example.slices.interfaces.LocationCallback() {
            @Override
            public void onSuccess(Location location) {
                // Set the location on EventInfo - this will extract and store coordinates
                eventInfo.setLocation(location);
                
                // Now create the event with coordinates
                // Note: pendingEventInfo will be cleared in createEventWithInfo
                createEventWithInfo(eventInfo);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), 
                    "Unable to get your location. Please enable location services and try again.", 
                    Toast.LENGTH_LONG).show();
                
                // Clear pending event info - do not create event without location
                pendingEventInfo = null;
            }
        });
    }

    /**
     * Navigates to the OrganizerEventsFragment after event creation.
     *
     */
    private void navigateToOrgEventsFragment() {
        NavController navController = NavHostFragment.findNavController(this);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build();

        navController.navigate(R.id.OrganizerEventsFragment, null, options);
    }
}
