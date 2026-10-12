package com.peakperformers.smartloadshedding.models;

/**
 * Display-shape model for one row on the Outage Schedule screen.
 * TODO: once the Express API + EskomSePush integration exists (FR-02/03),
 * this gets populated from a real network response instead of being
 * hand-built in OutageScheduleActivity. Field names are deliberately close
 * to what that API response will likely look like, to make that swap easy.
 */
public class OutageSchedule {
    public String dateLabel;   // e.g. "Tue 12 May"
    public String timeLabel;   // e.g. "18:00"
    public String durationLabel; // e.g. "2h"
    public int stage;          // 1-8, Eskom load shedding stage

    public OutageSchedule(String dateLabel, String timeLabel, String durationLabel, int stage) {
        this.dateLabel = dateLabel;
        this.timeLabel = timeLabel;
        this.durationLabel = durationLabel;
        this.stage = stage;
    }
}
