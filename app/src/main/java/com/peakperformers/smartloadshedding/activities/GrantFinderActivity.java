package com.peakperformers.smartloadshedding.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.models.Grant;

import java.util.ArrayList;
import java.util.List;

public class GrantFinderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grant_finder);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerGrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Grant> grants = new ArrayList<>();
        grants.add(new Grant("Small Business Energy Relief Fund", "SEDA",
                "15 June 2026", "Up to R50,000"));
        grants.add(new Grant("Township Enterprise Support", "SEFA",
                "30 July 2026", "Up to R30,000"));
        grants.add(new Grant("Green Energy Transition Grant", "DTIC",
                "1 September 2026", "Up to R100,000"));

        recyclerView.setAdapter(new GrantAdapter(grants));
    }
}
