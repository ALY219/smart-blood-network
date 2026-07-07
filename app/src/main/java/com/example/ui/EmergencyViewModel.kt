package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class EmergencyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db.appDao())

    // --- State Streams ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isRegistered = MutableStateFlow<Boolean>(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val activeRequests: StateFlow<List<EmergencyRequest>> = repository.activeRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHospitals: StateFlow<List<Hospital>> = repository.allHospitals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    // Interactive selections
    private val _selectedRequest = MutableStateFlow<EmergencyRequest?>(null)
    val selectedRequest: StateFlow<EmergencyRequest?> = _selectedRequest.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            // Pre-seed some hospitals and requests if empty
            repository.seedDefaultData()
            
            // Check if there is an existing registered user
            // We can search for any user in the table as our logged-in user for simulation
            val allDonorsList = repository.allDonors.firstOrNull() ?: emptyList()
            if (allDonorsList.isNotEmpty()) {
                val existing = allDonorsList.first()
                _currentUser.value = existing
                _isRegistered.value = true
            } else {
                // Check if any registered user is saved
                // Let's get the first user
                val defaultUser = repository.getUserById("user_current")
                if (defaultUser != null) {
                    _currentUser.value = defaultUser
                    _isRegistered.value = true
                }
            }
            _isLoading.value = false
        }
    }

    fun registerUser(name: String, role: String, bloodGroup: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Create a standard user with Lahore base coordinates
                val newUser = User(
                    uid = "user_current",
                    role = role,
                    name = name,
                    bloodGroup = bloodGroup,
                    latitude = 31.5204,
                    longitude = 74.3587,
                    isAvailable = true,
                    lastDonationDate = System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L,
                    fcmToken = "fcm_token_simulated"
                )
                repository.insertUser(newUser)
                _currentUser.value = newUser
                _isRegistered.value = true
                _toastMessage.emit("Successfully registered as $role!")
            } catch (e: Exception) {
                _toastMessage.emit("Failed to register: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.updateAvailability(user.uid, isAvailable)
                val updatedUser = user.copy(isAvailable = isAvailable)
                _currentUser.value = updatedUser
                _toastMessage.emit(
                    if (isAvailable) "You are now active & visible to hospitals nearby!"
                    else "Availability turned off."
                )
            } catch (e: Exception) {
                _toastMessage.emit("Error updating status: ${e.message}")
            }
        }
    }

    fun selectRequest(request: EmergencyRequest?) {
        _selectedRequest.value = request
    }

    fun broadcastRequest(
        patientName: String,
        bloodGroup: String,
        units: Int,
        hospital: Hospital
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newRequest = EmergencyRequest(
                    requestId = "req_${UUID.randomUUID().toString().take(6)}",
                    patientName = patientName,
                    bloodGroupRequired = bloodGroup,
                    units = units,
                    hospitalName = hospital.name,
                    latitude = hospital.latitude,
                    longitude = hospital.longitude,
                    status = "active",
                    timestamp = System.currentTimeMillis()
                )
                repository.insertRequest(newRequest)
                _toastMessage.emit("Urgent: Blood request broadcasted to all nearby donors!")
            } catch (e: Exception) {
                _toastMessage.emit("Error broadcasting request: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fulfillRequest(requestId: String) {
        viewModelScope.launch {
            try {
                repository.updateRequestStatus(requestId, "fulfilled")
                // Remove or complete
                repository.deleteRequest(requestId)
                _toastMessage.emit("Emergency blood request fulfilled successfully!")
                if (_selectedRequest.value?.requestId == requestId) {
                    _selectedRequest.value = null
                }
            } catch (e: Exception) {
                _toastMessage.emit("Error: ${e.message}")
            }
        }
    }

    fun acceptDonation(request: EmergencyRequest) {
        viewModelScope.launch {
            try {
                _toastMessage.emit("Thank you! Navigating you to ${request.hospitalName} for ${request.patientName}.")
                // Set the current selected request as the focused route
                _selectedRequest.value = request
            } catch (e: Exception) {
                _toastMessage.emit("Error responding to request: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _currentUser.value = null
            _isRegistered.value = false
        }
    }

    fun forceSeedLahoreData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.forceSeedData()
                _toastMessage.emit("Successfully seeded Lahore Logistics Network with 5 Hospitals and 3 Emergency requests!")
            } catch (e: Exception) {
                _toastMessage.emit("Failed to seed data: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
