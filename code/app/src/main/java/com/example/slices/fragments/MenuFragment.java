package com.example.slices.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.slices.MainActivity;
import com.example.slices.SharedViewModel;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.MenuFragmentBinding;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.InstanceUtil;
import com.example.slices.testing.DebugLogger;

public class  MenuFragment extends Fragment {
    private MenuFragmentBinding binding;

    private String name;
    private String email;
    private String phoneNumber;
    private boolean notifications;
    private DBConnector db;
    private SharedViewModel vm;

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

        vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        Entrant user =  vm.getUser();
        db = new DBConnector();
        name = user.getName();
        email = user.getEmail();
        phoneNumber = user.getPhoneNumber();
        notifications = user.getSendNotifications();

        binding.nameTextfield.setText(name);
        binding.emailTextfield.setText(email);
        binding.phoneNumberTextfield.setText(phoneNumber);
        binding.sendNotificationsSwitch.setChecked(notifications);

        if (((MainActivity) requireActivity()).getAppMode().equals("User")) {
            binding.appModeButtonGroup.check(binding.userModeButton.getId());
        } else {
            binding.appModeButtonGroup.check(binding.organizerModeButton.getId());
        }

        setUpClickListeners();
    }

    private void setUpClickListeners() {
        binding.profileEditButton.setOnClickListener(v -> onEditClicked());
        binding.profileCancelButton.setOnClickListener(v -> onCancelClicked());
        binding.profileSaveButton.setOnClickListener(v -> onSaveClicked());
        binding.organizerModeButton.setOnClickListener(v -> onOrganizerClicked());
        binding.userModeButton.setOnClickListener(v -> onUserClicked());
    }

    private void onEditClicked() {
        setProfileEditingEnabled(true);
    }

    /*
     * Reverts fields back to original and turns off editing
     */
    private void onCancelClicked() {
        setProfileEditingEnabled(false);

        binding.nameTextfield.setText(name);
        binding.emailTextfield.setText(email);
        binding.phoneNumberTextfield.setText(phoneNumber);
        binding.sendNotificationsSwitch.setChecked(notifications);
    }

    /*
     * Tries to save the user with entered information,
     * if unsuccessful, pops an error and stays in edit mode
     */
    private void onSaveClicked() {
        String newName = String.valueOf(binding.nameTextfield.getText());
        String newEmail = String.valueOf(binding.emailTextfield.getText());
        String newPhone = String.valueOf(binding.phoneNumberTextfield.getText());
        boolean newNotifications = binding.sendNotificationsSwitch.isChecked();

        Entrant currentUser = vm.getUser();

        if (newName.trim().isEmpty()) {
            binding.nameTextfield.setError("Name required");
            return;
        }
        if (newEmail.trim().isEmpty()) {
            binding.emailTextfield.setError("Email required");
            return;
        }

        Entrant newUser = new Entrant(newName, newEmail, newPhone, currentUser.getId());
        newUser.setDeviceId(InstanceUtil.getDeviceId((MainActivity) requireActivity()));
        newUser.setSendNotifications(newNotifications);
        setProfileEditingEnabled(false);
        binding.profileSaveButton.setEnabled(false);
        binding.profileCancelButton.setEnabled(false);

        db.updateEntrant(newUser, new DBWriteCallback() {
            @Override public void onSuccess() {
                vm.setUser(newUser);
                name = newName;
                email = newEmail;
                phoneNumber = newPhone;
                notifications = newNotifications;

                binding.profileSaveButton.setEnabled(true);
                binding.profileCancelButton.setEnabled(true);
                setProfileEditingEnabled(false);
            }

            @Override public void onFailure(Exception e) {
                binding.nameTextfield.setText(name);
                binding.emailTextfield.setText(email);
                binding.phoneNumberTextfield.setText(phoneNumber);
                binding.sendNotificationsSwitch.setChecked(notifications);

                setProfileEditingEnabled(true);
            }
        });
    }
    private void onOrganizerClicked() {
        ((MainActivity) requireActivity()).switchToOrganizer();
    }
    private void onUserClicked() {
        ((MainActivity) requireActivity()).switchToUser();
    }

    private void setProfileEditingEnabled(boolean enabled) {
        binding.nameTextfield.setEnabled(enabled);
        binding.emailTextfield.setEnabled(enabled);
        binding.phoneNumberTextfield.setEnabled(enabled);
        binding.sendNotificationsSwitch.setEnabled(enabled);

        binding.profileEditButton.setVisibility(enabled ? View.GONE : View.VISIBLE);
        binding.profileCancelButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        binding.profileSaveButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

