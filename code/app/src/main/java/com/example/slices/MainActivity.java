package com.example.slices;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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