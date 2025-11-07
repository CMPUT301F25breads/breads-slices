package com.example.slices.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.slices.R;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Event;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrganizerCreateEventFragment extends Fragment {

    private EditText eventNameInput, eventDescriptionInput, eventLocationInput;
    private EditText eventDateInput, regDeadlineInput, maxEntrantsInput;
    private Button createEventButton;
    private ImageButton backButton;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public OrganizerCreateEventFragment() {
        // Empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_create_event, container, false);

        // Link to layout components
        eventNameInput = view.findViewById(R.id.editEventName);
        eventDescriptionInput = view.findViewById(R.id.editDescription);
        eventLocationInput = view.findViewById(R.id.editLocation);
        eventDateInput = view.findViewById(R.id.editDate);
        regDeadlineInput = view.findViewById(R.id.input_registration_deadline);
        maxEntrantsInput = view.findViewById(R.id.input_max_entrants);
        createEventButton = view.findViewById(R.id.button_create_event);
        backButton = view.findViewById(R.id.backButton);

        // --- Date picker dialogs ---
        eventDateInput.setOnClickListener(v -> showDatePickerDialog(eventDateInput));
        regDeadlineInput.setOnClickListener(v -> showDatePickerDialog(regDeadlineInput));

        // --- Back button ---
        if (backButton != null) {
            backButton.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        // --- Create Event button ---
        createEventButton.setOnClickListener(v -> handleCreateEvent());

        return view;
    }

    private void showDatePickerDialog(EditText targetField) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String formattedDate = dateFormat.format(calendar.getTime());
                    targetField.setText(formattedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show();
    }

    private void handleCreateEvent() {
        String name = eventNameInput.getText().toString().trim();
        String description = eventDescriptionInput.getText().toString().trim();
        String location = eventLocationInput.getText().toString().trim();
        String eventDateStr = eventDateInput.getText().toString().trim();
        String regDeadlineStr = regDeadlineInput.getText().toString().trim();
        String maxEntrantsStr = maxEntrantsInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location)
                || TextUtils.isEmpty(eventDateStr) || TextUtils.isEmpty(regDeadlineStr)
                || TextUtils.isEmpty(maxEntrantsStr)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxEntrants;
        try {
            maxEntrants = Integer.parseInt(maxEntrantsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Max entrants must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Date eventDate = dateFormat.parse(eventDateStr);
            Date regDeadline = dateFormat.parse(regDeadlineStr);

            Timestamp eventTimestamp = new Timestamp(eventDate);
            Timestamp regDeadlineTimestamp = new Timestamp(regDeadline);

            new Event(name, description, location, eventTimestamp, regDeadlineTimestamp, maxEntrants, new EventCallback() {
                @Override
                public void onSuccess(Event event) {
                    Toast.makeText(getContext(), "Event created successfully!", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // Go back after success
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to create event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid date format. Please use MM/dd/yyyy", Toast.LENGTH_SHORT).show();
        }
    }
}