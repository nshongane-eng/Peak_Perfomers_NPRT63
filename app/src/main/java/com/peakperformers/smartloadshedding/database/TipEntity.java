package com.peakperformers.smartloadshedding.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tips")
public class TipEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "text")
    public String text = "";

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "synced")
    public boolean synced = false;
}