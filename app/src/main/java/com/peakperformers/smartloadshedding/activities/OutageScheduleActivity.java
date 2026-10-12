package com.peakperformers.smartloadshedding.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.models.OutageSchedule;

import java.util.ArrayList;
import java.util.List;

public class OutageScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outage_schedule);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerOutages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<OutageSchedule> schedules = new ArrayList<>();
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
        schedules.add(new OutageSchedule("Sat 17 May", "08:00", "2h", 2));
        schedules.add(new OutageSchedule("Sun 18 May", "10:00", "2h", 1));

        recyclerView.setAdapter(new OutageAdapter(schedules));
    }
}
