package com.example.slices.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.slices.R;

import java.util.Calendar;

public class OrganizerEditEventFragment extends Fragment {

    private EditText editEventName, editDate, editTime, editRegStart, editRegEnd,
            editMaxWaiting, editMaxParticipants, editMaxDistance;
    private TextView textDescription, textGuidelines, textLocation;
    private ImageView eventImage, qrCodeIcon;
    private SwitchCompat switchEntrantLocation;
    private LinearLayout layoutMaxDistance;

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
                editMaxDistance.setText(""); // clear if turned off (optional but clean)
            }
        });

        // --- load a placeholder image with Glide ---
        Glide.with(this)
                .load(R.drawable.ic_image)
                .placeholder(R.drawable.ic_image)
                .into(eventImage);

        // --- Date/Time pickers ---
        editDate.setOnClickListener(v -> showDatePickerDialog(editDate));
        editTime.setOnClickListener(v -> showTimePickerDialog(editTime));

        // --- Registration period pickers ---
        editRegStart.setOnClickListener(v -> showDatePickerDialog(editRegStart));
        editRegEnd.setOnClickListener(v -> showDatePickerDialog(editRegEnd));

        // --- Edit buttons (placeholders for now) ---
        buttonEditDescription.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Description clicked", Toast.LENGTH_SHORT).show());
        buttonEditGuidelines.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Guidelines clicked", Toast.LENGTH_SHORT).show());
        buttonEditLocation.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Location clicked", Toast.LENGTH_SHORT).show());
        buttonEditImage.setOnClickListener(v -> Toast.makeText(getContext(), "Edit Image clicked", Toast.LENGTH_SHORT).show());
        buttonViewWaitingList.setOnClickListener(v -> Toast.makeText(getContext(), "View Waiting List clicked", Toast.LENGTH_SHORT).show());
        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
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
}