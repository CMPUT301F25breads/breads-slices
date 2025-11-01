package com.example.slices;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slices.controllers.DBConnector;
import com.example.slices.databinding.ActivityMainBinding;
import com.example.slices.exceptions.EntrantNotFound;
import com.example.slices.interfaces.EntrantCallback;
import com.example.slices.models.Entrant;
import com.example.slices.models.InstanceUtil;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Entrant user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        initializeUser();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set listeners for bottom navigation buttons
        // Will all change soon
        binding.myEventsButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MyEventsFragment));
        binding.myEventsOrgButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MyEventsOrgFragment));
        binding.browseButton.setOnClickListener(view -> navController.navigate(R.id.action_to_BrowseFragment));
        binding.notifButton.setOnClickListener(view -> navController.navigate(R.id.action_to_NotifFragment));
        binding.menuButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MenuFragment));
        binding.createButton.setOnClickListener(view -> navController.navigate(R.id.action_to_CreateFragment));

        // When ready to move to menu fragment this listener can be used and delete from here
        //        binding.organizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        //            if (getActivity() != null) {
        //                ((MainActivity) getActivity()).toggleBar(isChecked);
        //            }
        //        });
        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleBar(isChecked);
        });
    }

    /**
     * Alternate visibility between the Organizer navigation view and Entrant
     * navigation view
     * @param isChecked
     *      boolean passed from a switch listener, true when switch is checked
     *      false when not
     */
    public void toggleBar(boolean isChecked) {
        binding.createButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        binding.myEventsOrgButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        binding.browseButton.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        binding.myEventsButton.setVisibility(isChecked ? View.GONE : View.VISIBLE);
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
                user = entrant;
                Toast.makeText(MainActivity.this, String.format("Hello %s", user.getDeviceId()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                if(e instanceof EntrantNotFound) {
                    new Entrant(deviceId, new EntrantCallback() {
                        @Override
                        public void onSuccess(Entrant entrant) {
                            user = entrant;
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });
                }
            }
        });
    }

    public Entrant getUser() {
        return user;
    }

}