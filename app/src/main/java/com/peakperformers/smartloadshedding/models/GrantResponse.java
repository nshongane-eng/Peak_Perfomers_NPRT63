package com.peakperformers.smartloadshedding.models;

import com.google.gson.annotations.SerializedName;

/**
 * Shape exactly as the Express API returns it from grant_listing.
 * Kept separate from the Grant display model used by GrantAdapter — that
 * one has pre-formatted display strings; this one is the raw wire shape.
 */
public class GrantResponse {

    @SerializedName("grant_id")
    public int grantId;

    public String name;

    public String provider;

    public String deadline; // arrives as "2026-11-15" (MySQL DATE, no time component)

    public String amount;

    public String province;
}