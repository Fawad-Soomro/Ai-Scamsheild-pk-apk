package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ScanRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {

    @Query("SELECT * FROM scan_records ORDER BY timestampMs DESC")
    fun getAllScans(): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Long): ScanRecord?

    @Query("SELECT * FROM scan_records WHERE id = :id LIMIT 1")
    fun observeScanById(id: Long): Flow<ScanRecord?>

    @Query("SELECT * FROM scan_records WHERE contentPreview LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY timestampMs DESC")
    fun searchScans(query: String): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE riskLevel = :riskLevel ORDER BY timestampMs DESC")
    fun getScansByRiskLevel(riskLevel: String): Flow<List<ScanRecord>>

    @Query("SELECT * FROM scan_records WHERE inputType = :inputType ORDER BY timestampMs DESC")
    fun getScansByInputType(inputType: String): Flow<List<ScanRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanRecord): Long

    @Query("DELETE FROM scan_records WHERE id = :id")
    suspend fun deleteScanById(id: Long)

    @Query("DELETE FROM scan_records")
    suspend fun deleteAllScans()

    @Query("SELECT COUNT(*) FROM scan_records")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_records WHERE riskLevel IN ('High', 'Critical')")
    fun getHighRiskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_records WHERE riskLevel = 'Moderate'")
    fun getModerateRiskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scan_records WHERE riskLevel = 'Low'")
    fun getLowRiskCount(): Flow<Int>
}
