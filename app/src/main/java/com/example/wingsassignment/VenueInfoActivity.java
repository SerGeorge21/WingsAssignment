package com.example.wingsassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class VenueInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_info);

        TextView name = (TextView) findViewById(R.id.infoTextView);
        String venueName = getIntent().getStringExtra("VENUE_INFO");
        name.setText(venueName);

    }
}