package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val role: String, // "Donor" or "Patient"
    val name: String,
    val bloodGroup: String,
    val latitude: Double,
    val longitude: Double,
    val isAvailable: Boolean = true,
    val lastDonationDate: Long = System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L, // default 90 days ago
    val fcmToken: String = ""
)

@Entity(tableName = "emergency_requests")
data class EmergencyRequest(
    @PrimaryKey val requestId: String,
    val patientName: String,
    val bloodGroupRequired: String,
    val units: Int,
    val hospitalName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String, // "active" or "fulfilled"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hospitals")
data class Hospital(
    @PrimaryKey val hospitalId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val contactInfo: String
)
