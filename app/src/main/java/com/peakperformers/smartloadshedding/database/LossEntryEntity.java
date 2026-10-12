package com.peakperformers.smartloadshedding.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room's local copy of FINANCIAL_LOSS. This is separate from
 * models.LossEntry on purpose: models.LossEntry is what gets sent
 * to/from the Express API (network shape), this Entity is what gets
 * stored on-device (database shape). They'll look similar but keeping
 * them as two classes means changing one doesn't silently break the other.
 */
@Entity(tableName = "loss_entries")
public class LossEntryEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "category")
    public String category = "";

    @ColumnInfo(name = "amount")
    public double amount;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "created_at")
    public long createdAt; // stored as epoch millis, System.currentTimeMillis()

    @ColumnInfo(name = "synced")
    public boolean synced = false; // becomes true once successfully POSTed to the Express API
}