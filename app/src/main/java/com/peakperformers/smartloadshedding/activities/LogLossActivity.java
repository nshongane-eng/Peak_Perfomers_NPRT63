package com.peakperformers.smartloadshedding.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.LossEntryEntity;
import com.peakperformers.smartloadshedding.models.LossEntry;
import com.peakperformers.smartloadshedding.network.ApiService;
import com.peakperformers.smartloadshedding.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogLossActivity extends AppCompatActivity {

    private TextView[] categoryChips;
    private String selectedCategory = null;
    private EditText etAmount, etNotes;

    private static final int HARDCODED_BUSINESS_ID = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_loss);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);
        Button btnReview = findViewById(R.id.btnReview);

        categoryChips = new TextView[]{
                findViewById(R.id.catLostSales),
                findViewById(R.id.catSpoiledStock),
                findViewById(R.id.catGeneratorFuel),
                findViewById(R.id.catIdleWages),
                findViewById(R.id.catEquipmentDamage),
                findViewById(R.id.catOther)
        };

        String[] categoryNames = {
                "Lost Sales", "Spoiled Stock", "Generator Fuel",
                "Idle Wages", "Equipment Damage", "Other"
        };

        for (int i = 0; i < categoryChips.length; i++) {
            TextView chip = categoryChips[i];
            String categoryName = categoryNames[i];
            chip.setOnClickListener(v -> selectCategory(chip, categoryName));
        }

        btnReview.setOnClickListener(v -> attemptSave());
    }

    private void selectCategory(TextView clickedChip, String categoryName) {
        selectedCategory = categoryName;
        for (TextView chip : categoryChips) {
            chip.setBackgroundTintList(null);
            chip.setTextColor(getColor(R.color.text_secondary));
        }
        clickedChip.setBackgroundTintList(getColorStateList(R.color.brand_orange));
        clickedChip.setTextColor(getColor(android.R.color.white));
    }

    private void attemptSave() {
        String amountText = etAmount.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (selectedCategory == null) {
            Toast.makeText(this, "Please pick a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(amountText)) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Amount must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        LossEntryEntity entity = new LossEntryEntity();
        entity.category = selectedCategory;
        entity.amount = amount;
        entity.notes = notes;
        entity.createdAt = System.currentTimeMillis();

        double finalAmount = amount;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            long newId = db.lossDao().insert(entity);

            runOnUiThread(() -> {
                Toast.makeText(LogLossActivity.this,
                        String.format("Loss saved: R%.2f (%s)", finalAmount, selectedCategory),
                        Toast.LENGTH_LONG).show();
                finish();
            });

            syncToServer(entity, (int) newId);
        });
    }

    private void syncToServer(LossEntryEntity entity, int localId) {
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String createdAtIso = isoFormat.format(new Date(entity.createdAt));

        LossEntry networkEntry = new LossEntry(
                HARDCODED_BUSINESS_ID,
                entity.category,
                entity.amount,
                entity.notes,
                createdAtIso
        );

        ApiService api = RetrofitClient.getApiService();
        api.logLoss(networkEntry).enqueue(new Callback<ApiService.LossResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiService.LossResponse> call,
                                    @NonNull Response<ApiService.LossResponse> response) {
                if (response.isSuccessful()) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                        db.lossDao().markSynced(localId);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiService.LossResponse> call, @NonNull Throwable t) {
                Log.e("LogLossActivity", "Sync failed, will retry later: " + t.getMessage());
            }
        });
    }
}
