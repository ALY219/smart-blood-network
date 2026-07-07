package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserById(uid: String): User?

    @Query("SELECT * FROM users WHERE role = 'Donor'")
    fun getAllDonors(): Flow<List<User>>

    @Query("UPDATE users SET isAvailable = :isAvailable WHERE uid = :uid")
    suspend fun updateAvailability(uid: String, isAvailable: Boolean)

    // --- Emergency Request Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyRequest(request: EmergencyRequest)

    @Query("SELECT * FROM emergency_requests ORDER BY timestamp DESC")
    fun getAllRequests(): Flow<List<EmergencyRequest>>

    @Query("SELECT * FROM emergency_requests WHERE status = 'active' ORDER BY timestamp DESC")
    fun getActiveRequests(): Flow<List<EmergencyRequest>>

    @Query("UPDATE emergency_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: String, status: String)

    @Query("DELETE FROM emergency_requests WHERE requestId = :requestId")
    suspend fun deleteRequest(requestId: String)

    // --- Hospital Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitals(hospitals: List<Hospital>)

    @Query("SELECT * FROM hospitals")
    fun getAllHospitals(): Flow<List<Hospital>>

    @Query("DELETE FROM emergency_requests")
    suspend fun deleteAllRequests()

    @Query("DELETE FROM hospitals")
    suspend fun deleteAllHospitals()
}
