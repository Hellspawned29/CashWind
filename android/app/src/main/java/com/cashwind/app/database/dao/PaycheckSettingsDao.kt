package com.cashwind.app.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cashwind.app.database.entity.PaycheckSettingsEntity

@Dao
interface PaycheckSettingsDao {
    @Query("SELECT * FROM paycheck_settings WHERE userId = :userId")
    fun getPaycheckSettingsLive(userId: Int): LiveData<PaycheckSettingsEntity?>

    @Query("SELECT * FROM paycheck_settings WHERE userId = :userId")
    suspend fun getPaycheckSettings(userId: Int): PaycheckSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePaycheckSettings(settings: PaycheckSettingsEntity)
}
