package com.peakperformers.smartloadshedding.activities;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.models.OutageSchedule;

import java.util.List;

public class OutageAdapter extends RecyclerView.Adapter<OutageAdapter.OutageViewHolder> {

    private final List<OutageSchedule> schedules;

    public OutageAdapter(List<OutageSchedule> schedules) {
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public OutageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outage_schedule, parent, false);
        return new OutageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OutageViewHolder holder, int position) {
        OutageSchedule item = schedules.get(position);

        Context context = holder.itemView.getContext();
        holder.tvOutageDate.setText(context.getString(R.string.outage_date_time, item.dateLabel, item.timeLabel));
        holder.tvOutageDuration.setText(context.getString(R.string.outage_duration_stage, item.durationLabel, item.stage));
        holder.tvOutageStage.setText(context.getString(R.string.outage_stage_short, item.stage));
        holder.tvOutageStage.setBackgroundTintList(
                ColorStateList.valueOf(colorForStage(item.stage)));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    // Higher stages = more severe = redder. Stages 1-3 stay orange (your brand
    // colour), 4-6 shift toward red, so the visual severity matches reality
    // without needing extra drawable files per stage.
    private int colorForStage(int stage) {
        if (stage <= 3) return Color.parseColor("#E8722C"); // brand_orange
        if (stage <= 5) return Color.parseColor("#D9534F"); // amber-red
        return Color.parseColor("#B91C1C");                 // deep red
    }

    public static class OutageViewHolder extends RecyclerView.ViewHolder {
        TextView tvOutageDate, tvOutageDuration, tvOutageStage;

        OutageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOutageDate = itemView.findViewById(R.id.tvOutageDate);
            tvOutageDuration = itemView.findViewById(R.id.tvOutageDuration);
            tvOutageStage = itemView.findViewById(R.id.tvOutageStage);
        }
    }
}
