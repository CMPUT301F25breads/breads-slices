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
    public void DBTester() {
        DBConnector db = new DBConnector();
        db.getEntrant("12345", new DBConnector.EntrantCallback() {
            @Override
            public void onSuccess(Entrant entrant) {
                System.out.println("Entrant found: " + entrant.getName());

            }
            @Override
            public void onFailure(Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }
}