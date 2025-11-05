package com.example.slices;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Admin_SignIn extends AppCompatActivity {

    private EditText usernameInput;
    private EditText passwordInput;
    private Button btnSignIn;

    // add credentials somehow either concrete or in firebase?
    // String = username
    //String2 = password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_signin); // matches your XML name

        // Link Java variables to XML components
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        btnSignIn = findViewById(R.id.btn_signin);

        // Handle the Sign In button click
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignIn();
            }
        });
    }

    /**
     * Validates the username and password
     */

    void handleSignIn() {

        String enteredUsername = usernameInput.getText().toString().trim();
        String enteredPassword = passwordInput.getText().toString().trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            Toast.makeText(this, "Please enter username and passord", Toast.LENGTH_SHORT).show();
            return;
        }
                //rework logic
        if (enteredUsername.equals(String) && enteredPassword.equals(String2)) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

            //admin_home.clas is the admin dashboard
            Intent intent = new Intent(Admin_SignIn.this, admin_home.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show();
        }

    }

}
