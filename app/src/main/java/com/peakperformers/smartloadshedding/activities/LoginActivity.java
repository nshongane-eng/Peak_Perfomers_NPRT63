package com.peakperformers.smartloadshedding.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.peakperformers.smartloadshedding.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        ImageButton btnBack = findViewById(R.id.btnBackLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBar);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvCreateAccount.setOnClickListener(v -> {
            String currentEmail = etEmail.getText().toString().trim();
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            if (!TextUtils.isEmpty(currentEmail)) {
                intent.putExtra("email", currentEmail);
            }
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            goToHome();
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    progressBar.setVisibility(View.GONE);
                    goToHome();
                })
                .addOnFailureListener(e -> handleLoginError(email, e));
    }

    private void handleLoginError(String email, Exception e) {
        progressBar.setVisibility(View.GONE);
        btnSignIn.setEnabled(true);

        boolean isNotRegistered = false;
        String message = e != null && e.getMessage() != null ? e.getMessage() : "";

        if (e instanceof FirebaseAuthInvalidUserException) {
            isNotRegistered = true;
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            String errorCode = ((FirebaseAuthInvalidCredentialsException) e).getErrorCode();
            if ("ERROR_USER_NOT_FOUND".equalsIgnoreCase(errorCode) ||
                message.toLowerCase().contains("user-not-found") ||
                message.toLowerCase().contains("user not found") ||
                message.toLowerCase().contains("no user record")) {
                isNotRegistered = true;
            } else {
                Toast.makeText(this, "Incorrect password or credentials. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }
        } else if (message.toLowerCase().contains("user-not-found") ||
                   message.toLowerCase().contains("user not found") ||
                   message.toLowerCase().contains("no user record")) {
            isNotRegistered = true;
        }

        if (isNotRegistered) {
            showAccountNotRegisteredDialog(email);
        } else {
            Toast.makeText(this, "Sign in failed: " + message, Toast.LENGTH_LONG).show();
        }
    }

    private void showAccountNotRegisteredDialog(String email) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Account Not Registered")
                .setMessage("No registered account was found for " + email + ".\n\nPlease create an account first to continue.")
                .setPositiveButton("Create Account", (dialog, which) -> {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void goToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
