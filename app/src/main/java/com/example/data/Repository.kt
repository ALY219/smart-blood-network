package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class Repository(private val appDao: AppDao) {

    val activeRequests: Flow<List<EmergencyRequest>> = appDao.getActiveRequests()
    val allHospitals: Flow<List<Hospital>> = appDao.getAllHospitals()
    val allRequests: Flow<List<EmergencyRequest>> = appDao.getAllRequests()
    val allDonors: Flow<List<User>> = appDao.getAllDonors()

    suspend fun insertUser(user: User) {
        appDao.insertUser(user)
    }

    suspend fun getUserById(uid: String): User? {
        return appDao.getUserById(uid)
    }

    suspend fun updateAvailability(uid: String, isAvailable: Boolean) {
        appDao.updateAvailability(uid, isAvailable)
    }

    suspend fun insertRequest(request: EmergencyRequest) {
        appDao.insertEmergencyRequest(request)
    }

    suspend fun updateRequestStatus(requestId: String, status: String) {
        appDao.updateRequestStatus(requestId, status)
    }

    suspend fun deleteRequest(requestId: String) {
        appDao.deleteRequest(requestId)
    }

    suspend fun seedDefaultData() {
        // Only seed hospitals if none exist
        val existingHospitals = appDao.getAllHospitals().firstOrNull()
        if (existingHospitals.isNullOrEmpty()) {
            val defaultHospitals = listOf(
                Hospital(
                    hospitalId = "hosp_1",
                    name = "Mayo Hospital",
                    address = "Hospital Road, Near Anarkali, Lahore",
                    latitude = 31.5723,
                    longitude = 74.3121,
                    contactInfo = "+92-42-99211102"
                ),
                Hospital(
                    hospitalId = "hosp_2",
                    name = "Lahore General Hospital",
                    address = "Ferozepur Road, Lahore",
                    latitude = 31.4429,
                    longitude = 74.3436,
                    contactInfo = "+92-42-99264031"
                ),
                Hospital(
                    hospitalId = "hosp_3",
                    name = "Doctors Hospital",
                    address = "152-G/1, Canal Bank Road, Johar Town, Lahore",
                    latitude = 31.4812,
                    longitude = 74.2721,
                    contactInfo = "+92-42-35307000"
                ),
                Hospital(
                    hospitalId = "hosp_4",
                    name = "Services Hospital",
                    address = "Ghaus-ul-Azam Road, Jail Road, Lahore",
                    latitude = 31.5428,
                    longitude = 74.3321,
                    contactInfo = "+92-42-99203402"
                ),
                Hospital(
                    hospitalId = "hosp_5",
                    name = "Shaukat Khanum Hospital",
                    address = "7A Block R-3, Johar Town, Lahore",
                    latitude = 31.4339,
                    longitude = 74.2635,
                    contactInfo = "+92-42-35905000"
                )
            )
            appDao.insertHospitals(defaultHospitals)
        }

        // Only seed emergency requests if none exist
        val existingRequests = appDao.getAllRequests().firstOrNull()
        if (existingRequests.isNullOrEmpty()) {
            val defaultRequests = listOf(
                EmergencyRequest(
                    requestId = "req_1",
                    patientName = "Ali Ahmed",
                    bloodGroupRequired = "O-",
                    units = 2,
                    hospitalName = "Mayo Hospital",
                    latitude = 31.5723,
                    longitude = 74.3121,
                    status = "active",
                    timestamp = System.currentTimeMillis() - 120 * 60 * 1000L
                ),
                EmergencyRequest(
                    requestId = "req_2",
                    patientName = "Zainab Bibi",
                    bloodGroupRequired = "A+",
                    units = 3,
                    hospitalName = "Doctors Hospital",
                    latitude = 31.4812,
                    longitude = 74.2721,
                    status = "active",
                    timestamp = System.currentTimeMillis() - 45 * 60 * 1000L
                ),
                EmergencyRequest(
                    requestId = "req_3",
                    patientName = "Fatima Hassan",
                    bloodGroupRequired = "B-",
                    units = 1,
                    hospitalName = "Services Hospital",
                    latitude = 31.5428,
                    longitude = 74.3321,
                    status = "active",
                    timestamp = System.currentTimeMillis() - 10 * 60 * 1000L
                ),
                EmergencyRequest(
                    requestId = "req_4",
                    patientName = "Muhammad Raza",
                    bloodGroupRequired = "AB+",
                    units = 4,
                    hospitalName = "Lahore General Hospital",
                    latitude = 31.4429,
                    longitude = 74.3436,
                    status = "active",
                    timestamp = System.currentTimeMillis() - 5 * 60 * 1000L
                )
            )
            for (req in defaultRequests) {
                appDao.insertEmergencyRequest(req)
            }
        }
    }

    suspend fun forceSeedData() {
        appDao.deleteAllRequests()
        appDao.deleteAllHospitals()

        val hospitals = listOf(
            Hospital(
                hospitalId = "hosp_seed_1",
                name = "City Central Hospital",
                address = "Central Plaza, Lahore",
                latitude = 31.5204 + 0.015,
                longitude = 74.3587 - 0.012,
                contactInfo = "+92-42-5550101"
            ),
            Hospital(
                hospitalId = "hosp_seed_2",
                name = "General Emergency Ward",
                address = "G.T. Road, Lahore",
                latitude = 31.5204 - 0.021,
                longitude = 74.3587 + 0.018,
                contactInfo = "+92-42-5550102"
            ),
            Hospital(
                hospitalId = "hosp_seed_3",
                name = "Aura Medical Clinic",
                address = "Gulberg III, Lahore",
                latitude = 31.5204 + 0.008,
                longitude = 74.3587 + 0.005,
                contactInfo = "+92-42-5550103"
            ),
            Hospital(
                hospitalId = "hosp_seed_4",
                name = "Lahore Children Hospital",
                address = "Ferozepur Road, Lahore",
                latitude = 31.5204 - 0.012,
                longitude = 74.3587 - 0.019,
                contactInfo = "+92-42-5550104"
            ),
            Hospital(
                hospitalId = "hosp_seed_5",
                name = "Jinnah Memorial Hospital",
                address = "Allama Iqbal Town, Lahore",
                latitude = 31.5204 + 0.024,
                longitude = 74.3587 - 0.028,
                contactInfo = "+92-42-5550105"
            )
        )
        appDao.insertHospitals(hospitals)

        val requests = listOf(
            EmergencyRequest(
                requestId = "req_seed_1",
                patientName = "Sajid Khan",
                bloodGroupRequired = "O-",
                units = 4,
                hospitalName = "General Emergency Ward",
                latitude = 31.5204 - 0.021,
                longitude = 74.3587 + 0.018,
                status = "active",
                timestamp = System.currentTimeMillis() - 5 * 60 * 1000L
            ),
            EmergencyRequest(
                requestId = "req_seed_2",
                patientName = "Zoya Fatima",
                bloodGroupRequired = "B+",
                units = 3,
                hospitalName = "City Central Hospital",
                latitude = 31.5204 + 0.015,
                longitude = 74.3587 - 0.012,
                status = "active",
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000L
            ),
            EmergencyRequest(
                requestId = "req_seed_3",
                patientName = "Asim Butt",
                bloodGroupRequired = "A-",
                units = 2,
                hospitalName = "Aura Medical Clinic",
                latitude = 31.5204 + 0.008,
                longitude = 74.3587 + 0.005,
                status = "active",
                timestamp = System.currentTimeMillis() - 2 * 60 * 1000L
            )
        )
        for (req in requests) {
            appDao.insertEmergencyRequest(req)
        }
    }
}
