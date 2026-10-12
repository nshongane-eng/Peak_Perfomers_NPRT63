package com.peakperformers.smartloadshedding.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.activities.TipAdapter;
import com.peakperformers.smartloadshedding.database.AppDatabase;
import com.peakperformers.smartloadshedding.database.TipEntity;
import com.peakperformers.smartloadshedding.models.Tip;
import com.peakperformers.smartloadshedding.network.ApiService;
import com.peakperformers.smartloadshedding.network.RetrofitClient;
import com.peakperformers.smartloadshedding.utils.EmptyStateHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityFragment extends Fragment {

    private static final int HARDCODED_BUSINESS_ID = 2;

    private RecyclerView recyclerView;
    private View emptyState;
    private EditText etNewTip, etSearch;
    private List<TipEntity> allTips = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_community_tips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerTips);
        emptyState = view.findViewById(R.id.emptyState);
        etNewTip = view.findViewById(R.id.etNewTip);
        etSearch = view.findViewById(R.id.etSearch);
        Button btnPostTip = view.findViewById(R.id.btnPostTip);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnPostTip.setOnClickListener(v -> postTip());
        setupSearch();

        loadTips();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterTips(String query) {
        List<TipEntity> filtered = new ArrayList<>();
        String q = query.toLowerCase().trim();

        for (TipEntity tip : allTips) {
            if (TextUtils.isEmpty(q) || (tip.text != null && tip.text.toLowerCase().contains(q))) {
                filtered.add(tip);
            }
        }

        recyclerView.setAdapter(new TipAdapter(filtered));
        EmptyStateHelper.apply(emptyState, recyclerView, filtered.isEmpty(),
                "💬", "No matching tips",
                "Try searching for a different keyword or post a new tip");
    }

    private void postTip() {
        String text = etNewTip.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(getContext(), "Write something first", Toast.LENGTH_SHORT).show();
            return;
        }

        TipEntity localTip = new TipEntity();
        localTip.text = text;
        localTip.createdAt = System.currentTimeMillis();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            long newId = db.tipDao().insert(localTip);

            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                etNewTip.setText("");
                loadTips();
            });

            syncTipToServer(text, (int) newId);
        });
    }

    private void syncTipToServer(String text, int localId) {
        Tip networkTip = new Tip(HARDCODED_BUSINESS_ID, text);

        ApiService api = RetrofitClient.getApiService();
        api.postTip(networkTip).enqueue(new Callback<ApiService.TipResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiService.TipResponse> call,
                                    @NonNull Response<ApiService.TipResponse> response) {
                if (response.isSuccessful()) {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
                        db.tipDao().markSynced(localId);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiService.TipResponse> call, @NonNull Throwable t) {
                Log.e("CommunityFragment", "Tip sync failed, will retry later: " + t.getMessage());
            }
        });
    }

    private void loadTips() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext());
            List<TipEntity> tips = db.tipDao().getAll();

            if (tips.isEmpty()) {
                String[] seedTips = {
                    "Bakery (Soweto): We bake our dough during stable grid hours and freeze unbaked pastries to save on generator diesel!",
                    "Retail Supermarket (Midrand): Motion-sensor solar emergency lights keep our store aisles safe and lit during evening stage 3 outages.",
                    "Tech Startup (Sandton): Using cloud-hosted billing systems lets our staff work remotely during unexpected stage 4 cuts.",
                    "Hair Salon (Rosebank): We use cordless rechargeable hair clippers and backup ring lights so clients never miss their appointments.",
                    "Cold Storage Logistics (Centurion): Adding phase-change thermal ice packs to our delivery trucks keeps perishables cold for 6+ hours without power.",
                    "Restaurant (Melville): We switched our gas stove tops to piezo-ignition pilot lights so we can continue cooking hot meals instantly when power drops.",
                    "Print & Copy Shop (Braamfontein): Investing in a small online UPS for our card machines and fiber router keeps transactions processing smoothly.",
                    "Automotive Workshop (Randburg): Air compressors are heavy inductive loads; we stagger our pneumatic tool usage so the inverter doesn't trip.",
                    "Coffee Roastery (Fourways): We roast in morning batches when the grid is stable and use hand-press grinders during afternoon load shedding slots.",
                    "Medical Practice (Pretoria): Portable vaccine and medicine cooler boxes powered by 12V car adapters save thousands in temperature-sensitive stock.",
                    "Boutique Clothing (Greenpoint): Using battery-powered rechargeable track lighting keeps window displays shining during afternoon outages.",
                    "Community Pharmacy (Durban): Keeping essential refrigeration on a dedicated solar inverter circuit prevents insulin and vaccine spoilage.",
                    "Solar Installers (Nelspruit): Always size your commercial inverter with a 30% surge headroom above peak motor startup loads.",
                    "Security & CCTV Co. (Port Elizabeth): Upgrading security gate motors to lithium backup batteries ensures business premises remain secure when power cuts.",
                    "Veterinary Clinic (Bloemfontein): Portable oxygen concentrators with built-in backup batteries are vital for animal surgeries during load shedding.",
                    "Car Wash (Polokwane): Staggering high-pressure water pumps and vacuums prevents main breaker overloads when grid power returns.",
                    "Guesthouse (Stellenbosch): Installing gas geysers ensures guests always have hot showers regardless of Eskom schedules.",
                    "Fitness Gym (Umhlanga): Using kinetic energy self-powered spinning bikes helps light up the cardio zone during unexpected blackouts.",
                    "Florist & Nursery (Pietermaritzburg): Automated drip irrigation timers with built-in backup cells protect delicate greenhouse seedlings.",
                    "Accounting Firm (East London): Automated cloud document backups every 30 minutes protect financial ledgers against sudden power surges."
                };

                long now = System.currentTimeMillis();
                for (int i = 0; i < seedTips.length; i++) {
                    TipEntity tip = new TipEntity();
                    tip.text = seedTips[i];
                    tip.createdAt = now - ((long) (seedTips.length - i) * 3600000L);
                    db.tipDao().insert(tip);
                }

                tips = db.tipDao().getAll();
            }

            allTips = tips;
            List<TipEntity> finalTips = tips;
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (isAdded()) {
                    String query = etSearch.getText().toString();
                    if (!TextUtils.isEmpty(query)) {
                        filterTips(query);
                    } else {
                        recyclerView.setAdapter(new TipAdapter(finalTips));
                        EmptyStateHelper.apply(emptyState, recyclerView, finalTips.isEmpty(),
                                "💬", "No tips yet",
                                "Be the first to share a coping tip with the community");
                    }
                }
            });
        });
    }
}
