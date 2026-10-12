package com.peakperformers.smartloadshedding.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.BusinessEntity;
import com.peakperformers.smartloadshedding.database.LossEntryEntity;
import com.peakperformers.smartloadshedding.utils.EmptyStateHelper;
import com.peakperformers.smartloadshedding.utils.ReportExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LossDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvTotalLosses;
    private Button btnFilterAll, btnFilterSales, btnFilterSpoiled, btnFilterFuel, btnFilterWages, btnFilterDamage;
    private String currentCategory = "All";
    private List<LossEntryEntity> cachedEntries = new ArrayList<>();
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loss_dashboard);

        firebaseAuth = FirebaseAuth.getInstance();

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        recyclerView = findViewById(R.id.recyclerLosses);
        emptyState = findViewById(R.id.emptyState);
        tvTotalLosses = findViewById(R.id.tvTotalLosses);

        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterSales = findViewById(R.id.btnFilterSales);
        btnFilterSpoiled = findViewById(R.id.btnFilterSpoiled);
        btnFilterFuel = findViewById(R.id.btnFilterFuel);
        btnFilterWages = findViewById(R.id.btnFilterWages);
        btnFilterDamage = findViewById(R.id.btnFilterDamage);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setupFilterButtons();

        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportLossesReport());
        }

        loadData();
    }

    private void exportLossesReport() {
        List<LossEntryEntity> filteredList = getFilteredEntries();
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No loss entries to export in this category", Toast.LENGTH_SHORT).show();
            return;
        }

        Context appContext = getApplicationContext();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                BusinessEntity business = null;
                if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
                    business = db.businessDao().getByUid(firebaseAuth.getCurrentUser().getUid());
                }
                final BusinessEntity finalBusiness = business;

                runOnUiThread(() -> ReportExporter.exportLossReport(LossDashboardActivity.this, filteredList, finalBusiness));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(LossDashboardActivity.this, "Export failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                executor.shutdown();
            }
        });
    }

    private List<LossEntryEntity> getFilteredEntries() {
        if (currentCategory.equals("All")) {
            return new ArrayList<>(cachedEntries);
        }
        List<LossEntryEntity> filtered = new ArrayList<>();
        for (LossEntryEntity entry : cachedEntries) {
            if (entry.category != null && entry.category.equalsIgnoreCase(currentCategory)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    private void setupFilterButtons() {
        View.OnClickListener listener = v -> {
            Button b = (Button) v;
            currentCategory = b.getText().toString();
            updateFilterButtonStyles(b);
            filterAndDisplayEntries();
        };

        if (btnFilterAll != null) btnFilterAll.setOnClickListener(listener);
        if (btnFilterSales != null) btnFilterSales.setOnClickListener(listener);
        if (btnFilterSpoiled != null) btnFilterSpoiled.setOnClickListener(listener);
        if (btnFilterFuel != null) btnFilterFuel.setOnClickListener(listener);
        if (btnFilterWages != null) btnFilterWages.setOnClickListener(listener);
        if (btnFilterDamage != null) btnFilterDamage.setOnClickListener(listener);
    }

    private void updateFilterButtonStyles(Button selectedButton) {
        Button[] buttons = {btnFilterAll, btnFilterSales, btnFilterSpoiled, btnFilterFuel, btnFilterWages, btnFilterDamage};
        int orangeColor = Color.parseColor("#E8722C");
        int navyColor = Color.parseColor("#101B2D");

        for (Button btn : buttons) {
            if (btn != null) {
                if (btn == selectedButton) {
                    btn.setBackgroundTintList(ColorStateList.valueOf(orangeColor));
                } else {
                    btn.setBackgroundTintList(ColorStateList.valueOf(navyColor));
                }
            }
        }
    }

    private void loadData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<LossEntryEntity> allEntries = db.lossDao().getAll();
                cachedEntries = allEntries;

                long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                Double total = db.lossDao().getTotalSince(sevenDaysAgo);
                double totalValue = Objects.requireNonNullElse(total, 0.0);

                runOnUiThread(() -> {
                    tvTotalLosses.setText(String.format(Locale.getDefault(), "R %.2f", totalValue));
                    filterAndDisplayEntries();
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    private void filterAndDisplayEntries() {
        List<LossEntryEntity> filtered = getFilteredEntries();
        recyclerView.setAdapter(new LossAdapter(filtered));
        EmptyStateHelper.apply(emptyState, recyclerView, filtered.isEmpty(),
                "💸", "No losses in this category",
                "Tap \"+ Log a loss\" on Home to record your first one");
    }
}
