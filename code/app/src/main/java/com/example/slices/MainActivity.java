package com.example.slices;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.ActivityMainBinding;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.interfaces.EventCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.Event;
import com.example.slices.models.InstanceUtil;
import com.example.slices.testing.DebugLogger;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedViewModel sharedViewModel;
    private String appMode; // Variable to store what mode the app is in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        // Start in User mode
        appMode = "User";

        initializeUser();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        NavigationUI.setupWithNavController(binding.bottomNavOrg, navController);
        NavigationUI.setupWithNavController(binding.bottomNavAdmin, navController);

//        String name = "Event" ;
//        String description = "Description" ;
//        String location = "Location" ;
//        Calendar cal = Calendar.getInstance();
//        cal.set(2025, 12, 12, 15, 0, 0);
//        Date date = cal.getTime();
//        Timestamp eventDate = new Timestamp(date);
//        cal.set(2025, 12, 12, 13, 0, 0);
//        Date date2 = cal.getTime();
//        Timestamp regDeadline = new Timestamp(date2);
//
//        new Event(name, description, location, eventDate, regDeadline, 5, new EventCallback() {
//            @Override
//            public void onSuccess(Event event) {
//
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                DebugLogger.d("TestUtils", "Failed to create event: " + e.getMessage());
//            }
//        });

    }

    /**
     * Switches visibility of Bottom Navigation Menu to enable User Mode
     */
    public void switchToUser() {
        appMode = "User";
        binding.bottomNav.setVisibility(View.VISIBLE);
        binding.bottomNavOrg.setVisibility(View.GONE);
        binding.bottomNavAdmin.setVisibility(View.GONE);
    }

    /**
     * Switches visibility of Bottom Navigation Menu to enable Organizer Mode
     */
    public void switchToOrganizer() {
        appMode = "Organizer";
        binding.bottomNav.setVisibility(View.GONE);
        binding.bottomNavOrg.setVisibility(View.VISIBLE);
        binding.bottomNavAdmin.setVisibility(View.GONE);
    }

    /**
     * Switches visibility of Bottom Navigation Menu to enable Admin Mode
     */
    public void switchToAdmin() {
        appMode = "Admin";
        binding.bottomNav.setVisibility(View.GONE);
        binding.bottomNavOrg.setVisibility(View.GONE);
        binding.bottomNavAdmin.setVisibility(View.VISIBLE);
    }

    public String getAppMode() {
        return appMode;
    }

    /**
     * Initialize the user information, either obtain entrant from firebase
     * if the deviceId exists, or create a new Entrant in firebase with the
     * deviceId
     */
    public void initializeUser() {
        String deviceId = InstanceUtil.getDeviceId(this);
        DBConnector db = new DBConnector();
        db.getEntrantByDeviceId(deviceId, new EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                sharedViewModel.setUser(entrant);
                Toast.makeText(MainActivity.this, String.format("Hello %s", entrant.getName()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                if(e instanceof EntrantNotFound) {
                    new Entrant(deviceId, new EntrantCallback() {
                        @Override
                        public void onSuccess(Entrant entrant) {
                            sharedViewModel.setUser(entrant);
                            NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_content_main);
                            navController.navigate(R.id.action_to_MenuFragment);
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                }
            }
        });
    }

}