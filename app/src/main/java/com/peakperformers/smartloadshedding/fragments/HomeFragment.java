package com.peakperformers.smartloadshedding.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.activities.GrantFinderActivity;
import com.peakperformers.smartloadshedding.activities.LogLossActivity;
import com.peakperformers.smartloadshedding.activities.LoginActivity;
import com.peakperformers.smartloadshedding.activities.LossDashboardActivity;
import com.peakperformers.smartloadshedding.activities.MainActivity;
import com.peakperformers.smartloadshedding.activities.OutageScheduleActivity;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.BusinessEntity;
import com.peakperformers.smartloadshedding.utils.NotificationHelper;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private TextView tvWeeklyLoss, tvWeeklyEntries;
    private FirebaseAuth firebaseAuth;
    private CountDownTimer countDownTimer;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    private final String[] survivalTips = {
        "Tip: Unplug heavy inductive motors and compressors before power returns to protect equipment against voltage surge spikes.",
        "Tip: Keep refrigerator and freezer doors tightly closed during outages. A closed commercial fridge maintains safe temperatures for up to 4 hours.",
        "Tip: Clean your generator's air filter monthly and test-run it weekly under a light load to ensure reliability during stage 4+ outages.",
        "Tip: Install automatic voltage regulators (AVRs) on sensitive point-of-sale (POS) terminals and card machines.",
        "Tip: Schedule high-power tasks (water heating, industrial washing) during stable grid windows to conserve UPS battery life.",
        "Tip: Document losses immediately with photos and timestamps in the Losses tab to streamline insurance or grant claims.",
        "Tip: Keep emergency backup lighting (rechargeable LED bulbs) plugged in so they activate instantly when power cuts."
    };
    private int currentTipIndex = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Whether granted or denied
                }
        );

        return inflater.inflate(R.layout.activity_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        askNotificationPermission();

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> Log.d("FCM_TOKEN", token));

        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        TextView btnHamburger = view.findViewById(R.id.btnHamburger);
        TextView btnSignOut = view.findViewById(R.id.btnSignOut);
        Button btnLogLoss = view.findViewById(R.id.btnLogLoss);
        tvWeeklyLoss = view.findViewById(R.id.tvWeeklyLoss);
        tvWeeklyEntries = view.findViewById(R.id.tvWeeklyEntries);
        TextView tvGrantsCount = view.findViewById(R.id.tvGrantsCount);
        LinearLayout cardThisWeek = view.findViewById(R.id.cardThisWeek);
        LinearLayout cardGrants = view.findViewById(R.id.cardGrants);
        LinearLayout cardCountdown = view.findViewById(R.id.cardCountdown);
        ViewFlipper alertFlipper = view.findViewById(R.id.alertFlipper);

        if (alertFlipper != null) {
            alertFlipper.setOnClickListener(v -> alertFlipper.showNext());
        }

        if (tvGrantsCount != null) {
            tvGrantsCount.setText(R.string.grants_available_count);
        }

        setupChecklist(view);
        setupSurvivalTip(view);
        setupReminderSwitch(view);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
                    BusinessEntity business = db.businessDao().getByUid(uid);

                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            if (business != null && business.name != null) {
                                tvWelcome.setText(getString(R.string.welcome_user, business.name));
                            } else {
                                tvWelcome.setText(getString(R.string.welcome_user, currentUser.getEmail()));
                            }
                        }
                    });
                } finally {
                    executor.shutdown();
                }
            });
        }

        btnHamburger.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnSignOut.setOnClickListener(v -> {
            firebaseAuth.signOut();
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });

        btnLogLoss.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LogLossActivity.class)));

        cardThisWeek.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LossDashboardActivity.class)));

        cardGrants.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), GrantFinderActivity.class)));

        cardCountdown.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), OutageScheduleActivity.class)));

        startCountdown(view);
    }

    private void setupReminderSwitch(View view) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("preparedness_prefs", Context.MODE_PRIVATE);
        SwitchCompat switchReminder = view.findViewById(R.id.switchReminder);

        boolean isReminderEnabled = prefs.getBoolean("reminder_enabled", false);
        switchReminder.setChecked(isReminderEnabled);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("reminder_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(getContext(), "15-min outage reminders enabled", Toast.LENGTH_SHORT).show();
                NotificationHelper.showNotification(
                        requireContext(),
                        "🔔 Outage Reminders Active",
                        "You will be alerted 15 minutes before scheduled outages."
                );
            } else {
                Toast.makeText(getContext(), "15-min outage reminders disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSurvivalTip(View view) {
        LinearLayout cardSurvivalTip = view.findViewById(R.id.cardSurvivalTip);
        TextView tvDailyTip = view.findViewById(R.id.tvDailyTip);

        currentTipIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % survivalTips.length;
        tvDailyTip.setText(survivalTips[currentTipIndex]);

        View.OnClickListener cycleTipListener = v -> {
            currentTipIndex = (currentTipIndex + 1) % survivalTips.length;
            tvDailyTip.setText(survivalTips[currentTipIndex]);
        };

        cardSurvivalTip.setOnClickListener(cycleTipListener);
        tvDailyTip.setOnClickListener(cycleTipListener);
    }

    private void setupChecklist(View view) {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("preparedness_prefs", Context.MODE_PRIVATE);

        CheckBox chkSurge = view.findViewById(R.id.chkSurge);
        CheckBox chkFuel = view.findViewById(R.id.chkFuel);
        CheckBox chkBattery = view.findViewById(R.id.chkBattery);
        CheckBox chkContacts = view.findViewById(R.id.chkContacts);
        CheckBox chkLogging = view.findViewById(R.id.chkLogging);
        ProgressBar progressResilience = view.findViewById(R.id.progressResilience);
        TextView tvResilienceScore = view.findViewById(R.id.tvResilienceScore);

        chkSurge.setChecked(prefs.getBoolean("surge", false));
        chkFuel.setChecked(prefs.getBoolean("fuel", false));
        chkBattery.setChecked(prefs.getBoolean("battery", false));
        chkContacts.setChecked(prefs.getBoolean("contacts", false));
        chkLogging.setChecked(prefs.getBoolean("logging", false));

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            prefs.edit()
                    .putBoolean("surge", chkSurge.isChecked())
                    .putBoolean("fuel", chkFuel.isChecked())
                    .putBoolean("battery", chkBattery.isChecked())
                    .putBoolean("contacts", chkContacts.isChecked())
                    .putBoolean("logging", chkLogging.isChecked())
                    .apply();

            updateResilienceScore(chkSurge, chkFuel, chkBattery, chkContacts, chkLogging, progressResilience, tvResilienceScore);
        };

        chkSurge.setOnCheckedChangeListener(listener);
        chkFuel.setOnCheckedChangeListener(listener);
        chkBattery.setOnCheckedChangeListener(listener);
        chkContacts.setOnCheckedChangeListener(listener);
        chkLogging.setOnCheckedChangeListener(listener);

        updateResilienceScore(chkSurge, chkFuel, chkBattery, chkContacts, chkLogging, progressResilience, tvResilienceScore);
    }

    private void updateResilienceScore(CheckBox s, CheckBox f, CheckBox b, CheckBox c, CheckBox l,
                                     ProgressBar bar, TextView tv) {
        int checkedCount = 0;
        if (s.isChecked()) checkedCount++;
        if (f.isChecked()) checkedCount++;
        if (b.isChecked()) checkedCount++;
        if (c.isChecked()) checkedCount++;
        if (l.isChecked()) checkedCount++;

        bar.setProgress(checkedCount);
        int percent = (checkedCount * 100) / 5;
        tv.setText(String.format(Locale.getDefault(), "Score: %d%%", percent));
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshWeeklyLossSummary();
    }

    private void startCountdown(View root) {
        TextView tvCountdown = root.findViewById(R.id.tvCountdown);
        TextView tvOutageDetails = root.findViewById(R.id.tvOutageDetails);
        long twoHoursInMillis = 2 * 60 * 60 * 1000L;

        countDownTimer = new CountDownTimer(twoHoursInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (1000 * 60 * 60);
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText(R.string.zero_timer);
            }
        }.start();

        tvOutageDetails.setText(R.string.sample_outage_details);
    }

    private void refreshWeeklyLossSummary() {
        if (getContext() == null) return;
        Context appContext = getContext().getApplicationContext();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                Double total = db.lossDao().getTotalSince(sevenDaysAgo);
                double totalValue = Objects.requireNonNullElse(total, 0.0);
                int entryCount = db.lossDao().getCountSince(sevenDaysAgo);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (isAdded() && tvWeeklyLoss != null && tvWeeklyEntries != null) {
                        tvWeeklyLoss.setText(String.format(Locale.getDefault(), "R %.0f", totalValue));
                        tvWeeklyEntries.setText(getString(entryCount == 1 ? R.string.entry_single : R.string.entry_plural, entryCount));
                    }
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
