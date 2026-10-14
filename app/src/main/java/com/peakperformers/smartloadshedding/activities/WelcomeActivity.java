package com.peakperformers.smartloadshedding.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Show splash screen animation if configured
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // If user is already authenticated, skip welcome and go straight to MainActivity
        if (firebaseAuth.getCurrentUser() != null) {
            goToHome();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btnSignIn = findViewById(R.id.btnSignInWelcome);
        Button btnRegister = findViewById(R.id.btnRegisterWelcome);
        Button btnDemo = findViewById(R.id.btnDemoWelcome);

        btnSignIn.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class)));

        if (btnDemo != null) {
            btnDemo.setOnClickListener(v -> enterDemoMode());
        }
    }

    private void enterDemoMode() {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            com.peakperformers.smartloadshedding.database.AppDatabase db =
                    com.peakperformers.smartloadshedding.database.AppDatabase.getInstance(getApplicationContext());
            if (db.businessDao().getFirst() == null) {
                com.peakperformers.smartloadshedding.database.BusinessEntity demo =
                        new com.peakperformers.smartloadshedding.database.BusinessEntity();
                demo.firebaseUid = "demo_guest_user";
                demo.name = "Peak Bakery & Cafe";
                demo.suburb = "Midrand";
                demo.businessType = "Bakery / Food Service";
                db.businessDao().insertOrUpdate(demo);
            }
            runOnUiThread(this::goToHome);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            goToHome();
        }
    }

    private void goToHome() {
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }
}
