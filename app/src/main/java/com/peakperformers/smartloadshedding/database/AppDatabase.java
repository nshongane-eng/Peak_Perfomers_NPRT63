package com.peakperformers.smartloadshedding.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LossEntryEntity.class, TipEntity.class, BusinessEntity.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LossDao lossDao();
    public abstract TipDao tipDao();
    public abstract BusinessDao businessDao();

    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smart_load_shedding.db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
