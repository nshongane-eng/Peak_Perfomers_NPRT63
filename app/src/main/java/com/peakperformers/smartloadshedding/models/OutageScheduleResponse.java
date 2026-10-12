package com.peakperformers.smartloadshedding.models;

import com.google.gson.annotations.SerializedName;

/**
 * Shape of one row exactly as the Express API returns it (raw MySQL column
 * names). Kept separate from OutageSchedule (the display model used by
 * OutageAdapter) on purpose — this is the network/DB shape, that one is the
 * UI shape. ScheduleFragment converts between them.
 */
public class OutageScheduleResponse {

    @SerializedName("schedule_id")
    public int scheduleId;

    @SerializedName("area_code")
    public String areaCode;

    public int stage;

    @SerializedName("start_time")
    public String startTime; // arrives as a MySQL datetime string, e.g. "2026-09-29T18:00:00.000Z"

    @SerializedName("end_time")
    public String endTime;
}