package com.peakperformers.smartloadshedding.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.activities.LossAdapter;
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

public class LossesFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvTotalLosses;
    private Button btnFilterAll, btnFilterSales, btnFilterSpoiled, btnFilterFuel, btnFilterWages, btnFilterDamage;
    private Button btnExportCsv;
    private String currentCategory = "All";
    private List<LossEntryEntity> cachedEntries = new ArrayList<>();
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_loss_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recyclerLosses);
        emptyState = view.findViewById(R.id.emptyState);
        tvTotalLosses = view.findViewById(R.id.tvTotalLosses);

        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterSales = view.findViewById(R.id.btnFilterSales);
        btnFilterSpoiled = view.findViewById(R.id.btnFilterSpoiled);
        btnFilterFuel = view.findViewById(R.id.btnFilterFuel);
        btnFilterWages = view.findViewById(R.id.btnFilterWages);
        btnFilterDamage = view.findViewById(R.id.btnFilterDamage);
        btnExportCsv = view.findViewById(R.id.btnExportCsv);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupFilterButtons();

        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(v -> exportLossesReport());
        }
    }

    private void exportLossesReport() {
        if (getContext() == null) return;
        Context appContext = getContext().getApplicationContext();

        List<LossEntryEntity> filteredList = getFilteredEntries();
        if (filteredList.isEmpty()) {
            Toast.makeText(getContext(), "No loss entries to export in this category", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                BusinessEntity business = null;
                if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
                    business = db.businessDao().getByUid(firebaseAuth.getCurrentUser().getUid());
                }
                final BusinessEntity finalBusiness = business;

                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (isAdded() && getContext() != null) {
                        ReportExporter.exportLossReport(requireContext(), filteredList, finalBusiness);
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Export failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
        int orangeColor = Color.parseColor("#E8722C"); // brand_orange
        int navyColor = Color.parseColor("#101B2D");   // brand_navy

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

    @Override
    public void onResume() {
        super.onResume();
        loadLosses();
    }

    private void loadLosses() {
        if (getContext() == null) return;
        Context appContext = getContext().getApplicationContext();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(appContext);
                List<LossEntryEntity> entries = db.lossDao().getAll();

                if (entries.isEmpty()) {
                    long now = System.currentTimeMillis();
                    LossEntryEntity l1 = new LossEntryEntity();
                    l1.category = "Spoiled Stock";
                    l1.amount = 1850.0;
                    l1.notes = "Fridge failure during stage 4";
                    l1.createdAt = now - 86400000L;
                    db.lossDao().insert(l1);

                    LossEntryEntity l2 = new LossEntryEntity();
                    l2.category = "Generator Fuel";
                    l2.amount = 950.0;
                    l2.notes = "Diesel for 8-hour outage";
                    l2.createdAt = now - 172800000L;
                    db.lossDao().insert(l2);

                    LossEntryEntity l3 = new LossEntryEntity();
                    l3.category = "Lost Sales";
                    l3.amount = 3400.0;
                    l3.notes = "Closed during morning cut";
                    l3.createdAt = now - 259200000L;
                    db.lossDao().insert(l3);

                    entries = db.lossDao().getAll();
                }

                cachedEntries = entries;

                long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
                Double total = db.lossDao().getTotalSince(sevenDaysAgo);
                double totalValue = Objects.requireNonNullElse(total, 0.0);

                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        tvTotalLosses.setText(String.format(Locale.getDefault(), "R %.2f", totalValue));
                        filterAndDisplayEntries();
                    }
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
                "Try selecting 'All' or log a new loss");
    }
}
