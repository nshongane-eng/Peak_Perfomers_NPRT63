package com.peakperformers.smartloadshedding.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView tvCountdown, tvOutageDetails;
    private TextView tvWeeklyLoss, tvWeeklyEntries;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView btnSignOut = findViewById(R.id.btnSignOut);
        Button btnLogLoss = findViewById(R.id.btnLogLoss);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvOutageDetails = findViewById(R.id.tvOutageDetails);
        ViewFlipper alertFlipper = findViewById(R.id.alertFlipper);

        if (alertFlipper != null) {
            alertFlipper.setOnClickListener(v -> alertFlipper.showNext());
        }

        LinearLayout cardThisWeek = findViewById(R.id.cardThisWeek);
        LinearLayout cardGrants = findViewById(R.id.cardGrants);
        tvWeeklyLoss = findViewById(R.id.tvWeeklyLoss);
        tvWeeklyEntries = findViewById(R.id.tvWeeklyEntries);
        TextView tvGrantsCount = findViewById(R.id.tvGrantsCount);

        tvGrantsCount.setText(R.string.grants_available_count);

        findViewById(R.id.cardCountdown).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, OutageScheduleActivity.class)));

        cardThisWeek.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LossDashboardActivity.class)));

        cardGrants.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, GrantFinderActivity.class)));

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            tvWelcome.setText(getString(R.string.welcome_user, currentUser.getEmail()));
        }

        tvWelcome.setOnLongClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, CommunityTipsActivity.class));
            return true;
        });

        btnSignOut.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnLogLoss.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LogLossActivity.class)));

        startCountdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWeeklyLossSummary();
    }

    private void refreshWeeklyLossSummary() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());

                long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                Double total = db.lossDao().getTotalSince(sevenDaysAgo);
                double totalValue = Objects.requireNonNullElse(total, 0.0);

                int entryCount = db.lossDao().getCountSince(sevenDaysAgo);

                runOnUiThread(() -> {
                    tvWeeklyLoss.setText(String.format(Locale.getDefault(), "R %.0f", totalValue));
                    tvWeeklyEntries.setText(getString(entryCount == 1 ? R.string.entry_single : R.string.entry_plural, entryCount));
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    private void startCountdown() {
        long twoHoursInMillis = 2 * 60 * 60 * 1000L;

        countDownTimer = new CountDownTimer(twoHoursInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                tvCountdown.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText(R.string.zero_timer);
                tvOutageDetails.setText(R.string.outage_starting_now);
            }
        }.start();

        tvOutageDetails.setText(R.string.sample_outage_details);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
