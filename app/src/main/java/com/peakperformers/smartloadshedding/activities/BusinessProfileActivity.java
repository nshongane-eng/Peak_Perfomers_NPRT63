package com.peakperformers.smartloadshedding.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.BusinessEntity;

import java.util.concurrent.Executors;

public class BusinessProfileActivity extends AppCompatActivity {

    private EditText etBusinessName, etSuburb, etBusinessType;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_profile);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etBusinessName = findViewById(R.id.etBusinessName);
        etSuburb = findViewById(R.id.etSuburb);
        etBusinessType = findViewById(R.id.etBusinessType);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            loadProfile();
        }

        btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            BusinessEntity business = db.businessDao().getByUid(uid);

            if (business != null) {
                runOnUiThread(() -> {
                    if (business.name != null) etBusinessName.setText(business.name);
                    if (business.suburb != null) etSuburb.setText(business.suburb);
                    if (business.businessType != null) etBusinessType.setText(business.businessType);
                });
            }
        });
    }

    private void saveProfile() {
        String name = etBusinessName.getText().toString().trim();
        String suburb = etSuburb.getText().toString().trim();
        String type = etBusinessType.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter business name", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            BusinessEntity entity = new BusinessEntity();
            entity.firebaseUid = uid;
            entity.name = name;
            entity.suburb = suburb;
            entity.businessType = type;

            db.businessDao().insertOrUpdate(entity);

            runOnUiThread(() -> {
                Toast.makeText(this, "Business profile saved successfully!", Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}
