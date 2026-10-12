package com.peakperformers.smartloadshedding.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.TipEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommunityTipsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etNewTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_tips);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerTips);
        etNewTip = findViewById(R.id.etNewTip);
        Button btnPostTip = findViewById(R.id.btnPostTip);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnPostTip.setOnClickListener(v -> postTip());

        loadTips();
    }

    private void postTip() {
        String text = etNewTip.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Write something first", Toast.LENGTH_SHORT).show();
            return;
        }

        TipEntity tip = new TipEntity();
        tip.text = text;
        tip.createdAt = System.currentTimeMillis();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                db.tipDao().insert(tip);

                runOnUiThread(() -> {
                    etNewTip.setText("");
                    loadTips();
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    private void loadTips() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                List<TipEntity> tips = db.tipDao().getAll();

                runOnUiThread(() -> recyclerView.setAdapter(new TipAdapter(tips)));
            } finally {
                executor.shutdown();
            }
        });
    }
}
