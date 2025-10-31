package com.example.slices;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.slices.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        binding.textView.setText(android_id);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set listeners for bottom navigation buttons
        binding.myEventsButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MyEventsFragment));
        binding.myEventsOrgButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MyEventsOrgFragment));
        binding.browseButton.setOnClickListener(view -> navController.navigate(R.id.action_to_BrowseFragment));
        binding.notifButton.setOnClickListener(view -> navController.navigate(R.id.action_to_NotifFragment));
        binding.menuButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MenuFragment));
        binding.createButton.setOnClickListener(view -> navController.navigate(R.id.action_to_CreateFragment));

        // When ready to move to menu fragment this listener can be used and delete from here
        //        binding.organizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
        //            if (getActivity() != null) {
        //                ((MainActivity) getActivity()).toggleButtons(isChecked);
        //            }
        //        });
        binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleButtons(isChecked);
        });
    }

    public void toggleButtons(boolean isChecked) {
        binding.createButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        binding.myEventsOrgButton.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        binding.browseButton.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        binding.myEventsButton.setVisibility(isChecked ? View.GONE : View.VISIBLE);
    }


}