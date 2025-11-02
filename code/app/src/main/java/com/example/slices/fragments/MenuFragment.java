package com.example.slices.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.MainActivity;
import com.example.slices.SharedViewModel;
import com.example.slices.databinding.MenuFragmentBinding;
import com.example.slices.models.Entrant;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class  MenuFragment extends Fragment {
    private MenuFragmentBinding binding;

    private String name;
    private String email;
    private String phoneNumber;
    private boolean notifications;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = MenuFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Entrant user = vm.getUser();

        name = user.getName();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        notifications = true;

        binding.nameTextfield.setText(name);
        binding.emailTextfield.setText(email);
        binding.phoneNumberTextfield.setText(phoneNumber);
        binding.sendNotificationsSwitch.setChecked(notifications);

        binding.appModeButtonGroup.check(binding.userModeButton.getId());

        binding.profileEditButton.setOnClickListener(v -> {
            binding.nameTextfield.setEnabled(true);
            binding.emailTextfield.setEnabled(true);
            binding.phoneNumberTextfield.setEnabled(true);
            binding.sendNotificationsSwitch.setEnabled(true);

            binding.profileEditButton.setVisibility(View.GONE);
            binding.profileCancelButton.setVisibility(View.VISIBLE);
            binding.profileSaveButton.setVisibility(View.VISIBLE);
        });

        binding.profileCancelButton.setOnClickListener(v -> {
            binding.nameTextfield.setEnabled(false);
            binding.emailTextfield.setEnabled(false);
            binding.phoneNumberTextfield.setEnabled(false);
            binding.sendNotificationsSwitch.setEnabled(false);

            binding.nameTextfield.setText(name);
            binding.emailTextfield.setText(email);
            binding.phoneNumberTextfield.setText(phoneNumber);
            binding.sendNotificationsSwitch.setChecked(notifications);

            binding.profileEditButton.setVisibility(View.VISIBLE);
            binding.profileCancelButton.setVisibility(View.GONE);
            binding.profileSaveButton.setVisibility(View.GONE);
        });

        binding.profileSaveButton.setOnClickListener(v -> {
            binding.nameTextfield.setEnabled(false);
            binding.emailTextfield.setEnabled(false);
            binding.phoneNumberTextfield.setEnabled(false);
            binding.sendNotificationsSwitch.setEnabled(false);

            name = String.valueOf(binding.nameTextfield.getText());
            email = String.valueOf(binding.emailTextfield.getText());
            phoneNumber = String.valueOf(binding.phoneNumberTextfield.getText());
            notifications = binding.sendNotificationsSwitch.isChecked();

            binding.profileEditButton.setVisibility(View.VISIBLE);
            binding.profileCancelButton.setVisibility(View.GONE);
            binding.profileSaveButton.setVisibility(View.GONE);
        });

        binding.appModeButtonGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == binding.organizerModeButton.getId()) {
                    // Organizer mode selected
                    Log.d("ModeToggle", "Organizer mode selected");
                    ((MainActivity) getActivity()).switchToOrganizer();
                } else if (checkedId == binding.userModeButton.getId()) {
                    // User mode selected
                    Log.d("ModeToggle", "User mode selected");
                    ((MainActivity) getActivity()).switchToUser();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

