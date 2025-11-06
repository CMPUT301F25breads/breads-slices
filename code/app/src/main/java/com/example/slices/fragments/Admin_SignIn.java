package com.example.slices.fragments;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.slices.R;
import com.example.slices.fragments.EventDetailsFragment;



public class Admin_SignIn extends Fragment {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button btnSignIn;

    // add credentials somehow either concrete or in firebase?
    String admin_username = "username";
    String admin_password = "password";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.admin_signin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Link views to layout
        usernameInput = view.findViewById(R.id.username_input);
        passwordInput = view.findViewById(R.id.password_input);
        btnSignIn = view.findViewById(R.id.btn_signin);

        btnSignIn.setOnClickListener(v -> handleSignIn(view));
    }

    /**
     * Validates the username and password
     */

    private void handleSignIn(View view) {
        String enteredUsername = usernameInput.getText().toString().trim();
        String enteredPassword = passwordInput.getText().toString().trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredUsername.equals(admin_username) && enteredPassword.equals(admin_password)) {
            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();

            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.adminEventsFragment);

        } else {
            Toast.makeText(requireContext(), "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
