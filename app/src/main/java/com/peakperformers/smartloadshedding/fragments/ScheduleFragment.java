package com.peakperformers.smartloadshedding.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.activities.OutageAdapter;
import com.peakperformers.smartloadshedding.models.OutageSchedule;
import com.peakperformers.smartloadshedding.utils.EmptyStateHelper;

import java.util.ArrayList;
import java.util.List;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView tvLocationSubtitle;
    private Button btnLocMidrand, btnLocSandton, btnLocCenturion, btnLocPretoria, btnLocCapeTown, btnLocDurban;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_outage_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerOutages);
        emptyState = view.findViewById(R.id.emptyState);
        tvLocationSubtitle = view.findViewById(R.id.tvLocationSubtitle);

        btnLocMidrand = view.findViewById(R.id.btnLocMidrand);
        btnLocSandton = view.findViewById(R.id.btnLocSandton);
        btnLocCenturion = view.findViewById(R.id.btnLocCenturion);
        btnLocPretoria = view.findViewById(R.id.btnLocPretoria);
        btnLocCapeTown = view.findViewById(R.id.btnLocCapeTown);
        btnLocDurban = view.findViewById(R.id.btnLocDurban);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupLocationButtons();
        loadScheduleForLocation("Midrand");
    }

    private void setupLocationButtons() {
        View.OnClickListener listener = v -> {
            Button b = (Button) v;
            String loc = b.getText().toString();
            updateLocationButtonStyles(b);
            loadScheduleForLocation(loc);
        };

        btnLocMidrand.setOnClickListener(listener);
        btnLocSandton.setOnClickListener(listener);
        btnLocCenturion.setOnClickListener(listener);
        btnLocPretoria.setOnClickListener(listener);
        btnLocCapeTown.setOnClickListener(listener);
        btnLocDurban.setOnClickListener(listener);
    }

    private void updateLocationButtonStyles(Button selectedButton) {
        Button[] buttons = {btnLocMidrand, btnLocSandton, btnLocCenturion, btnLocPretoria, btnLocCapeTown, btnLocDurban};
        int orangeColor = Color.parseColor("#E8722C"); // brand_orange
        int navyColor = Color.parseColor("#1B2A4A");   // brand_navy

        for (Button btn : buttons) {
            if (btn == selectedButton) {
                btn.setBackgroundTintList(ColorStateList.valueOf(orangeColor));
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(navyColor));
            }
        }
    }

    private void loadScheduleForLocation(String location) {
        tvLocationSubtitle.setText("Showing schedule for: " + location + getRegionSuffix(location));
        List<OutageSchedule> displayList = getSchedulesForLocation(location);

        recyclerView.setAdapter(new OutageAdapter(displayList));
        EmptyStateHelper.apply(emptyState, recyclerView, displayList.isEmpty(),
                "☀️", "No outages scheduled",
                "Nothing planned for your area right now");
    }

    private String getRegionSuffix(String location) {
        switch (location.toLowerCase()) {
            case "sandton":
            case "midrand": return ", JHB";
            case "centurion":
            case "pretoria": return ", PTA";
            case "cape town": return ", WC";
            case "durban": return ", KZN";
            default: return "";
        }
    }

    private List<OutageSchedule> getSchedulesForLocation(String location) {
        List<OutageSchedule> schedules = new ArrayList<>();
        switch (location.toLowerCase()) {
            case "sandton":
                schedules.add(new OutageSchedule("Mon 12 May", "08:00", "2h", 3));
                schedules.add(new OutageSchedule("Tue 13 May", "16:00", "2.5h", 4));
                schedules.add(new OutageSchedule("Wed 14 May", "10:00", "2h", 3));
                schedules.add(new OutageSchedule("Thu 15 May", "14:00", "2h", 4));
                schedules.add(new OutageSchedule("Fri 16 May", "08:00", "3h", 5));
                schedules.add(new OutageSchedule("Sat 17 May", "12:00", "2h", 3));
                schedules.add(new OutageSchedule("Sun 18 May", "18:00", "2h", 2));
                break;
            case "centurion":
                schedules.add(new OutageSchedule("Mon 12 May", "04:00", "2h", 2));
                schedules.add(new OutageSchedule("Tue 13 May", "12:00", "2h", 2));
                schedules.add(new OutageSchedule("Wed 14 May", "18:00", "2.5h", 3));
                schedules.add(new OutageSchedule("Thu 15 May", "06:00", "2h", 2));
                schedules.add(new OutageSchedule("Fri 16 May", "20:00", "2h", 3));
                schedules.add(new OutageSchedule("Sat 17 May", "10:00", "2h", 1));
                break;
            case "pretoria":
                schedules.add(new OutageSchedule("Mon 12 May", "10:00", "3h", 4));
                schedules.add(new OutageSchedule("Tue 13 May", "20:00", "2h", 3));
                schedules.add(new OutageSchedule("Wed 14 May", "08:00", "2h", 2));
                schedules.add(new OutageSchedule("Thu 15 May", "16:00", "3h", 4));
                schedules.add(new OutageSchedule("Fri 16 May", "12:00", "2h", 3));
                schedules.add(new OutageSchedule("Sun 18 May", "14:00", "2h", 3));
                break;
            case "cape town":
                schedules.add(new OutageSchedule("Mon 12 May", "06:00", "2h", 1));
                schedules.add(new OutageSchedule("Tue 13 May", "14:00", "2h", 2));
                schedules.add(new OutageSchedule("Wed 14 May", "22:00", "2h", 1));
                schedules.add(new OutageSchedule("Thu 15 May", "10:00", "2h", 2));
                schedules.add(new OutageSchedule("Fri 16 May", "18:00", "2h", 1));
                schedules.add(new OutageSchedule("Sat 17 May", "08:00", "2h", 1));
                break;
            case "durban":
                schedules.add(new OutageSchedule("Mon 12 May", "12:00", "2h", 2));
                schedules.add(new OutageSchedule("Tue 13 May", "08:00", "2h", 1));
                schedules.add(new OutageSchedule("Wed 14 May", "16:00", "2.5h", 3));
                schedules.add(new OutageSchedule("Thu 15 May", "06:00", "2h", 2));
                schedules.add(new OutageSchedule("Fri 16 May", "14:00", "2h", 2));
                schedules.add(new OutageSchedule("Sun 18 May", "10:00", "2h", 1));
                break;
            case "midrand":
            default:
                schedules.add(new OutageSchedule("Mon 12 May", "06:00", "2h", 2));
                schedules.add(new OutageSchedule("Mon 12 May", "14:00", "2h", 3));
                schedules.add(new OutageSchedule("Tue 13 May", "08:00", "2.5h", 4));
                schedules.add(new OutageSchedule("Tue 13 May", "18:00", "2h", 3));
                schedules.add(new OutageSchedule("Wed 14 May", "04:00", "2h", 1));
                schedules.add(new OutageSchedule("Wed 14 May", "12:00", "3h", 5));
                schedules.add(new OutageSchedule("Thu 15 May", "10:00", "2h", 2));
                schedules.add(new OutageSchedule("Thu 15 May", "20:00", "2.5h", 4));
                schedules.add(new OutageSchedule("Fri 16 May", "06:00", "2h", 3));
                schedules.add(new OutageSchedule("Fri 16 May", "16:00", "4h", 6));
                break;
        }
        return schedules;
    }
}
