package com.example.slices;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.ActivityMainBinding;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.InstanceUtil;

/**
 * MainActivity View that sets up navigation for the bottom navigation bars
 * and gets the entrant object for the entrant in the firebase that has
 * a matching deviceId field
 * @author Brad
 */
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