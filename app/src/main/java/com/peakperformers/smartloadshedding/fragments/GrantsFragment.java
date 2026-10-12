package com.peakperformers.smartloadshedding.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.activities.GrantAdapter;
import com.peakperformers.smartloadshedding.models.Grant;
import com.peakperformers.smartloadshedding.models.GrantResponse;
import com.peakperformers.smartloadshedding.network.RetrofitClient;
import com.peakperformers.smartloadshedding.utils.EmptyStateHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrantsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private EditText etSearch;
    private List<Grant> allGrants = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_grant_finder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerGrants);
        emptyState = view.findViewById(R.id.emptyState);
        etSearch = view.findViewById(R.id.etSearch);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupSearch();
        loadGrants();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGrants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterGrants(String query) {
        List<Grant> filtered = new ArrayList<>();
        String q = query.toLowerCase().trim();

        for (Grant grant : allGrants) {
            boolean matches = TextUtils.isEmpty(q) ||
                    (grant.name != null && grant.name.toLowerCase().contains(q)) ||
                    (grant.provider != null && grant.provider.toLowerCase().contains(q)) ||
                    (grant.amount != null && grant.amount.toLowerCase().contains(q));

            if (matches) {
                filtered.add(grant);
            }
        }

        recyclerView.setAdapter(new GrantAdapter(filtered));
        EmptyStateHelper.apply(emptyState, recyclerView, filtered.isEmpty(),
                "🎁", "No matching grants found",
                "Try searching for a different keyword or provider");
    }

    private void loadGrants() {
        RetrofitClient.getApiService().getGrants().enqueue(new Callback<List<GrantResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<GrantResponse>> call,
                                    @NonNull Response<List<GrantResponse>> response) {
                List<Grant> displayList = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null) {
                    displayList = convertToDisplayModel(response.body());
                }

                if (displayList.isEmpty()) {
                    displayList = getFallbackGrants();
                }

                allGrants = displayList;
                String query = etSearch.getText().toString();
                if (!TextUtils.isEmpty(query)) {
                    filterGrants(query);
                } else {
                    recyclerView.setAdapter(new GrantAdapter(displayList));
                    EmptyStateHelper.apply(emptyState, recyclerView, displayList.isEmpty(),
                            "🎁", "No grants available",
                            "Check back later for new funding opportunities");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GrantResponse>> call, @NonNull Throwable t) {
                Log.w("GrantsFragment", "Could not reach server, using fallback grants: " + t.getMessage());
                List<Grant> displayList = getFallbackGrants();
                allGrants = displayList;

                String query = etSearch.getText().toString();
                if (!TextUtils.isEmpty(query)) {
                    filterGrants(query);
                } else {
                    recyclerView.setAdapter(new GrantAdapter(displayList));
                    EmptyStateHelper.apply(emptyState, recyclerView, false,
                            "🎁", "No grants available",
                            "Check back later for new funding opportunities");
                }
            }
        });
    }

    private List<Grant> getFallbackGrants() {
        List<Grant> grants = new ArrayList<>();
        grants.add(new Grant("Small Business Energy Relief Fund", "SEDA",
                "15 June 2026", "Up to R50,000"));
        grants.add(new Grant("Township Enterprise Support", "SEFA",
                "30 July 2026", "Up to R30,000"));
        grants.add(new Grant("Green Energy Transition Grant", "DTIC",
                "1 September 2026", "Up to R100,000"));
        grants.add(new Grant("IDC Commercial Solar & Inverter Financing", "IDC",
                "15 October 2026", "Up to R250,000"));
        grants.add(new Grant("Eskom Business Resilience & Backup Voucher", "Eskom",
                "30 November 2026", "Up to R25,000"));
        return grants;
    }

    private List<Grant> convertToDisplayModel(List<GrantResponse> apiRows) {
        List<Grant> result = new ArrayList<>();
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat displayFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

        for (GrantResponse row : apiRows) {
            String deadlineDisplay = row.deadline;
            try {
                if (row.deadline != null) {
                    Date parsed = apiFormat.parse(row.deadline);
                    if (parsed != null) {
                        deadlineDisplay = displayFormat.format(parsed);
                    }
                }
            } catch (ParseException e) {
                Log.e("GrantsFragment", "Failed to parse deadline: " + row.deadline);
            }

            result.add(new Grant(row.name, row.provider, deadlineDisplay, row.amount));
        }
        return result;
    }
}
