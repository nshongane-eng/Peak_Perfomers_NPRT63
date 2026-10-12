package com.peakperformers.smartloadshedding.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TipDao {

    @Insert
    long insert(TipEntity tip);

    @Query("SELECT * FROM tips ORDER BY created_at DESC")
    List<TipEntity> getAll();

    @Query("UPDATE tips SET synced = 1 WHERE id = :id")
    void markSynced(int id);
}