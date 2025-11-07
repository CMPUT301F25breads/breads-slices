package com.example.slices.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import com.example.slices.models.Event;
import com.example.slices.interfaces.EventCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrganizerEditEventFragment extends Fragment {

    private EditText editEventName, editDate, editTime, editRegStart, editRegEnd,
            editMaxWaiting, editMaxParticipants, editMaxDistance;
    private TextView textDescription, textGuidelines, textLocation;
    private ImageView eventImage, qrCodeIcon;
    private SwitchCompat switchEntrantLocation;
    private LinearLayout layoutMaxDistance;
    private DBConnector dbConnector;
    private String eventID; // event being edited

    public OrganizerEditEventFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_edit_event_fragment, container, false);
    }

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
        qrCodeIcon = view.findViewById(R.id.qrCodeIcon);
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

        // --- Other buttons (functionality not implemented yet)---
        buttonEditImage.setOnClickListener(v ->
                Toast.makeText(getContext(), "Edit Image clicked", Toast.LENGTH_SHORT).show()
        );

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        // --- Load event data ---
        eventID = getArguments() != null ? getArguments().getString("eventID") : null;
        if (eventID != null) {
            loadEventData(eventID);
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads event data from Firestore using DBConnector
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

    private void navigateToEntrantsFragment(Bundle bundle) {
        NavController navController = NavHostFragment.findNavController(this);

        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, false) // stay within the same tab
                .setLaunchSingleTop(true)           // prevent duplicates
                .setRestoreState(true)              // restore fragment state if needed
                .build();

        navController.navigate(R.id.action_OrganizerEditEventFragment_to_EventEntrantsFragment, bundle, options);
    }
}