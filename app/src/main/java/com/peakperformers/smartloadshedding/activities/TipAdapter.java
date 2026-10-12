package com.peakperformers.smartloadshedding.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.TipEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder> {

    private final List<TipEntity> tips;

    public TipAdapter(List<TipEntity> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        TipEntity tip = tips.get(position);
        holder.tvTipText.setText(tip.text);

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault());
        holder.tvTipDate.setText(sdf.format(tip.createdAt));
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipText, tvTipDate;

        TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipText = itemView.findViewById(R.id.tvTipText);
            tvTipDate = itemView.findViewById(R.id.tvTipDate);
        }
    }
}
