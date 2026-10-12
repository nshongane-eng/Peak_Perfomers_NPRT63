package com.peakperformers.smartloadshedding.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LossDao {

    @Insert
    long insert(LossEntryEntity entry); // returns the new row's id

    @Query("SELECT * FROM loss_entries ORDER BY created_at DESC")
    List<LossEntryEntity> getAll();

    @Query("SELECT SUM(amount) FROM loss_entries WHERE created_at >= :sinceMillis")
    Double getTotalSince(long sinceMillis);

    @Query("SELECT COUNT(*) FROM loss_entries WHERE created_at >= :sinceMillis")
    int getCountSince(long sinceMillis);

    @Query("SELECT * FROM loss_entries WHERE synced = 0")
    List<LossEntryEntity> getUnsynced();

    @Query("UPDATE loss_entries SET synced = 1 WHERE id = :id")
    void markSynced(int id);
}