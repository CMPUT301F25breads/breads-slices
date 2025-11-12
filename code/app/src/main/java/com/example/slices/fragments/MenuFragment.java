package com.example.slices.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.slices.MainActivity;
import com.example.slices.R;
import com.example.slices.SharedViewModel;
import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.MenuFragmentBinding;
import com.example.slices.interfaces.DBWriteCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.InstanceUtil;
import com.example.slices.testing.DebugLogger;
import com.example.slices.fragments.Admin_SignIn;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


/**
 * Fragment that displays and edits the current user's profile, and lets the user
 * switch between User, Organizer, and Admin modes. Also provides navigation to the
 * Admin sign-in screen.
 * @author Bhupinder Singh
 */
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

        // Sets the text fields to the current user's information
        binding.nameTextfield.setText(name);
        binding.emailTextfield.setText(email);
        binding.phoneNumberTextfield.setText(phoneNumber);
        binding.sendNotificationsSwitch.setChecked(notifications);

        // Sets the app mode button to the current app mode
        if (((MainActivity) requireActivity()).getAppMode().equals("User")) {
            binding.appModeButtonGroup.check(binding.userModeButton.getId());
        } else {
            binding.appModeButtonGroup.check(binding.organizerModeButton.getId());
        }

        setUpClickListeners();
    }


    /**
     * Sets up the click listeners for the buttons
     */
    private void setUpClickListeners() {
        binding.profileEditButton.setOnClickListener(v -> onEditClicked());
        binding.profileCancelButton.setOnClickListener(v -> onCancelClicked());
        binding.profileSaveButton.setOnClickListener(v -> onSaveClicked());
        binding.organizerModeButton.setOnClickListener(v -> onOrganizerClicked());
        binding.userModeButton.setOnClickListener(v -> onUserClicked());
        binding.adminModeButton.setOnClickListener(v -> onAdminClicked());
        binding.adminSigninButton.setOnClickListener(v -> onAdminSignInClicked());
        binding.deleteProfileButton.setOnClickListener(v -> onDeleteProfileClicked());
    }

    private void onEditClicked() {
        setProfileEditingEnabled(true);
    }

    /**
     * Reverts fields back to original and turns off editing
     */
    private void onCancelClicked() {
        setProfileEditingEnabled(false);

        binding.nameTextfield.setText(name);
        binding.emailTextfield.setText(email);
        binding.phoneNumberTextfield.setText(phoneNumber);
        binding.sendNotificationsSwitch.setChecked(notifications);
    }

    /**
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

        //Creates a new Entrant object with the new information and the current user's ID
        Entrant newUser = new Entrant(newName, newEmail, newPhone, currentUser.getId());
        newUser.setDeviceId(InstanceUtil.getDeviceId((MainActivity) requireActivity()));
        newUser.setSendNotifications(newNotifications);
        setProfileEditingEnabled(false);
        binding.profileSaveButton.setEnabled(false);
        binding.profileCancelButton.setEnabled(false);

        // Updates the user in the database
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
                Log.e("MenuFragment", "Error updating profile", e);
                Toast.makeText(requireContext(), "Error updating profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void onAdminSignInClicked() {
        NavController navController = NavHostFragment.findNavController(this);

        // Options to avoid breaking the bottom nav bar stack
        NavOptions options = new NavOptions.Builder()
                .setRestoreState(true)
                .setPopUpTo(R.id.nav_graph, false)
                .build();

        navController.navigate(R.id.adminSignInFragment, null, options);
//        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
//        navController.navigate(R.id.adminSignInFragment);
    }

    private void onAdminClicked() {
        ((MainActivity) requireActivity()).switchToAdmin();
    };
    private void onOrganizerClicked() {
        ((MainActivity) requireActivity()).switchToOrganizer();
    }
    private void onUserClicked() { ((MainActivity) requireActivity()).switchToUser(); }

    private void onDeleteProfileClicked() {
        new MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
                .setTitle("Are you sure you want to delete your profile?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("Yes", (dialogInterface, i) -> deleteProfile())
                .show();
    }

    private void deleteProfile() {
    }

    /**
     * Enables or disables the profile editing fields
     * @param enabled: true if the fields should be enabled, false otherwise
     */
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

