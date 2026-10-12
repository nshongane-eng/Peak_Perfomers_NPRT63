package com.peakperformers.smartloadshedding.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.database.LossEntryEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LossAdapter extends RecyclerView.Adapter<LossAdapter.LossViewHolder> {

    private final List<LossEntryEntity> entries;

    public LossAdapter(List<LossEntryEntity> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public LossViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loss_entry, parent, false);
        return new LossViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LossViewHolder holder, int position) {
        LossEntryEntity entry = entries.get(position);

        holder.tvItemCategory.setText(entry.category);
        holder.tvItemAmount.setText(String.format(Locale.getDefault(), "R %.2f", entry.amount));

        SimpleDateFormat sdf = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault());
        String dateText = sdf.format(entry.createdAt);
        holder.tvItemDate.setText(
                entry.notes != null && !entry.notes.isEmpty()
                        ? dateText + " · " + entry.notes
                        : dateText
        );

        // Mouse primary left click interaction
        holder.itemView.setOnClickListener(v ->
                Snackbar.make(v, entry.category + ": R " + String.format(Locale.getDefault(), "%.2f", entry.amount), Snackbar.LENGTH_SHORT).show());

        // Mouse secondary right click / context click interaction
        holder.itemView.setOnContextClickListener(v -> {
            String noteText = entry.notes != null && !entry.notes.isEmpty() ? "\nNotes: " + entry.notes : "";
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle(entry.category + " Entry")
                    .setMessage("Amount: R " + String.format(Locale.getDefault(), "%.2f", entry.amount) +
                            "\nDate: " + dateText + noteText)
                    .setPositiveButton("Close", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class LossViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemCategory, tvItemDate, tvItemAmount;

        LossViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemCategory = itemView.findViewById(R.id.tvItemCategory);
            tvItemDate = itemView.findViewById(R.id.tvItemDate);
            tvItemAmount = itemView.findViewById(R.id.tvItemAmount);
        }
    }
}
