package com.peakperformers.smartloadshedding.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Local copy of the BUSINESS table. TODO: once the Express API exists,
 * add a `synced` flag (same pattern as LossEntryEntity) and POST this to
 * MySQL's business table, keyed against the user's firebase_uid.
 */
@Entity(tableName = "business_profile")
public class BusinessEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "firebase_uid")
    public String firebaseUid = ""; // one profile per logged-in user, keyed by their Firebase UID

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "suburb")
    public String suburb;

    @ColumnInfo(name = "business_type")
    public String businessType;
}
