package com.peakperformers.smartloadshedding.models;

/**
 * TODO: once the Express API's grant endpoint exists (FR-06/07), this gets
 * populated from GET /api/grants?province=...&turnover=... instead of being
 * hand-built here. Fields mirror the GRANT table from the Phase 3 ERD.
 */
public class Grant {
    public String name;
    public String provider;   // SEDA, SEFA, DTIC
    public String deadline;   // display string, e.g. "15 June 2026"
    public String amount;     // display string, e.g. "Up to R50,000"

    public Grant(String name, String provider, String deadline, String amount) {
        this.name = name;
        this.provider = provider;
        this.deadline = deadline;
        this.amount = amount;
    }
}
