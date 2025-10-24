package com.example.slices;

import android.os.Bundle;

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

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Set listeners for bottom navigation buttons
        binding.myEventsButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MyEventsFragment));
        binding.browseButton.setOnClickListener(view -> navController.navigate(R.id.action_to_BrowseFragment));
        binding.notifButton.setOnClickListener(view -> navController.navigate(R.id.action_to_NotifFragment));
        binding.menuButton.setOnClickListener(view -> navController.navigate(R.id.action_to_MenuFragment));


    }

    //Tester for database

    public void writeTester() {
        DBConnector db = new DBConnector();
        Entrant entrant = new Entrant("John Doe", "will.henry.harrison@example-pet-store.com", "123-456-7890", 12345);
        if (db.writeEntrant(entrant)) {
            System.out.println("Entrant written to database");
        } else {
            System.out.println("Error writing entrant to database");
        }
    }


}