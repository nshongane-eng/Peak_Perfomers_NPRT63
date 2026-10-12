package com.peakperformers.smartloadshedding.models;

import com.google.gson.annotations.SerializedName;

/**
 * Shape sent to / received from the Express API (POST /api/losses,
 * GET /api/losses). Kept separate from database.LossEntryEntity on
 * purpose — that one is Room's on-device shape, this one is the wire
 * shape. LogLossActivity converts between them.
 */
public class LossEntry {

    @SerializedName("business_id")
    public int businessId;

    public String category;

    public double amount;

    public String notes;

    @SerializedName("created_at")
    public String createdAt; // ISO-8601 string, e.g. "2026-09-27T10:00:00"

    public LossEntry() { }

    public LossEntry(int businessId, String category, double amount, String notes, String createdAt) {
        this.businessId = businessId;
        this.category = category;
        this.amount = amount;
        this.notes = notes;
        this.createdAt = createdAt;
    }
}