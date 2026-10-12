package com.peakperformers.smartloadshedding.models;

import com.google.gson.annotations.SerializedName;

/**
 * Shape sent to / received from the Express API (POST /api/tips, GET
 * /api/tips). Kept separate from database.TipEntity — that's Room's
 * on-device shape, this is the wire shape.
 */
public class Tip {

    @SerializedName(value = "businessId", alternate = {"business_id"})
    public int businessId;

    public String text;

    @SerializedName(value = "createdAt", alternate = {"created_at"})
    public String createdAt;

    public Tip() { }

    public Tip(int businessId, String text) {
        this.businessId = businessId;
        this.text = text;
    }
}