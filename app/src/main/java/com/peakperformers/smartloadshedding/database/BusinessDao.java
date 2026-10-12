package com.peakperformers.smartloadshedding.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BusinessDao {

    // REPLACE means: if a row with this firebaseUid already exists, overwrite
    // it instead of throwing a duplicate-key error. Useful later if you ever
    // let users edit their profile (FR-10, Profile Management).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(BusinessEntity business);

    @Query("SELECT * FROM business_profile WHERE firebase_uid = :uid LIMIT 1")
    BusinessEntity getByUid(String uid);
}
