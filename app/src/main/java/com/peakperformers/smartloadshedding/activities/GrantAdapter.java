package com.peakperformers.smartloadshedding.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peakperformers.smartloadshedding.R;
import com.peakperformers.smartloadshedding.models.Grant;

import java.util.List;

public class GrantAdapter extends RecyclerView.Adapter<GrantAdapter.GrantViewHolder> {

    private final List<Grant> grants;

    public GrantAdapter(List<Grant> grants) {
        this.grants = grants;
    }

    @NonNull
    @Override
    public GrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grant, parent, false);
        return new GrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GrantViewHolder holder, int position) {
        Grant grant = grants.get(position);
        holder.tvGrantProvider.setText(grant.provider);
        holder.tvGrantName.setText(grant.name);
        holder.tvGrantAmount.setText(grant.amount);
        holder.tvGrantDeadline.setText(holder.itemView.getContext().getString(R.string.grant_deadline, grant.deadline));
    }

    @Override
    public int getItemCount() {
        return grants.size();
    }

    public static class GrantViewHolder extends RecyclerView.ViewHolder {
        TextView tvGrantProvider, tvGrantName, tvGrantAmount, tvGrantDeadline;

        GrantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGrantProvider = itemView.findViewById(R.id.tvGrantProvider);
            tvGrantName = itemView.findViewById(R.id.tvGrantName);
            tvGrantAmount = itemView.findViewById(R.id.tvGrantAmount);
            tvGrantDeadline = itemView.findViewById(R.id.tvGrantDeadline);
        }
    }
}
