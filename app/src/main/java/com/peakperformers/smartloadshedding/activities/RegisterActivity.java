package com.peakperformers.smartloadshedding.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.BusinessEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Spinner spinnerSuburb, spinnerBusinessType;
    private Button btnCreateAccount;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBackRegister);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        spinnerSuburb = findViewById(R.id.spinnerSuburb);
        spinnerBusinessType = findViewById(R.id.spinnerBusinessType);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progressBar = findViewById(R.id.progressBar);
        TextView tvSignIn = findViewById(R.id.tvSignIn);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String prefilledEmail = getIntent().getStringExtra("email");
        if (!TextUtils.isEmpty(prefilledEmail)) {
            etEmail.setText(prefilledEmail);
        }

        // Fill both dropdowns from the string-arrays in strings.xml
        ArrayAdapter<CharSequence> suburbAdapter = ArrayAdapter.createFromResource(
                this, R.array.suburbs_array, android.R.layout.simple_spinner_item);
        suburbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSuburb.setAdapter(suburbAdapter);

        ArrayAdapter<CharSequence> businessAdapter = ArrayAdapter.createFromResource(
                this, R.array.business_type_array, android.R.layout.simple_spinner_item);
        businessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBusinessType.setAdapter(businessAdapter);

        btnCreateAccount.setOnClickListener(v -> attemptRegister());
        tvSignIn.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String suburb = spinnerSuburb.getSelectedItem().toString();
        String businessType = spinnerBusinessType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in name, email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (suburb.startsWith("Select") || businessType.startsWith("Select")) {
            Toast.makeText(this, "Please choose your suburb and business type", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateAccount.setEnabled(false);

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    progressBar.setVisibility(View.GONE);

                    BusinessEntity business = new BusinessEntity();
                    if (result.getUser() != null) {
                        business.firebaseUid = result.getUser().getUid();
                    }
                    business.name = name;
                    business.suburb = suburb;
                    business.businessType = businessType;

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> {
                        try {
                            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                            db.businessDao().insertOrUpdate(business);

                            runOnUiThread(() -> {
                                Toast.makeText(RegisterActivity.this, "Account created! Welcome, " + name, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            });
                        } finally {
                            executor.shutdown();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateAccount.setEnabled(true);
                    Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
