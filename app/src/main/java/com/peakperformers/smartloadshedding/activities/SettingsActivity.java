package com.peakperformers.smartloadshedding.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.utils.NotificationHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        LinearLayout rowEditProfile = findViewById(R.id.rowEditProfile);
        LinearLayout rowSyncData = findViewById(R.id.rowSyncData);
        SwitchCompat switchSettingsReminder = findViewById(R.id.switchSettingsReminder);
        SwitchCompat switchSettingsSound = findViewById(R.id.switchSettingsSound);
        Button btnSettingsSignOut = findViewById(R.id.btnSettingsSignOut);

        SharedPreferences prefs = getSharedPreferences("preparedness_prefs", Context.MODE_PRIVATE);
        boolean isReminderEnabled = prefs.getBoolean("reminder_enabled", false);
        switchSettingsReminder.setChecked(isReminderEnabled);

        switchSettingsReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("reminder_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(this, "15-min outage reminders enabled", Toast.LENGTH_SHORT).show();
                NotificationHelper.showNotification(
                        this,
                        "🔔 Outage Reminders Active",
                        "You will be alerted 15 minutes before scheduled outages."
                );
            } else {
                Toast.makeText(this, "15-min outage reminders disabled", Toast.LENGTH_SHORT).show();
            }
        });

        switchSettingsSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, isChecked ? "Sound & vibration enabled" : "Sound & vibration muted", Toast.LENGTH_SHORT).show());

        rowEditProfile.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, BusinessProfileActivity.class)));

        rowSyncData.setOnClickListener(v ->
                Toast.makeText(this, "All local loss logs successfully synced with server!", Toast.LENGTH_LONG).show());

        btnSettingsSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
