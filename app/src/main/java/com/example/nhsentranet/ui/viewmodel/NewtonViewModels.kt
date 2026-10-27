package com.example.nhsentranet.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhsentranet.data.repository.NewtonRepository
import com.example.nhsentranet.network.UserSessionDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

// Sealed interface enforces robust compile-time UI routing and patterns
sealed interface ScreenState {
    object Idle : ScreenState
    object Loading : ScreenState
    data class Success(val message: String) : ScreenState
    data class Failure(val error: String) : ScreenState
}

// -------------------------------------------------------------------------
// AUTHENTICATION VIEWMODEL
// -------------------------------------------------------------------------
class AuthViewModel(private val repository: NewtonRepository) : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    
    private val _authState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val authState: StateFlow<ScreenState> get() = _authState

    private val _session = MutableStateFlow<UserSessionDto?>(null)
    val session: StateFlow<UserSessionDto?> get() = _session

    fun login() {
        if (username.isBlank() || password.length < 8) {
            _authState.value = ScreenState.Failure("Fields must not be blank, and password must be at least 8 characters.")
            return
        }

        _authState.value = ScreenState.Loading
        viewModelScope.launch {
            repository.login(username, password)
                .onSuccess { activeSession ->
                    _session.value = activeSession
                    _authState.value = ScreenState.Success("Welcome, ${activeSession.fullName}!")
                }
                .onFailure { t ->
                    _authState.value = ScreenState.Failure(t.message ?: "Authentication failed")
                }
        }
    }

    fun logout() {
        _session.value = null
        _authState.value = ScreenState.Idle 
        username = ""
        password = ""
    }
}

// -------------------------------------------------------------------------
// BILLING / PAYMENT VIEWMODEL
// -------------------------------------------------------------------------
class InvoicesViewModel(private val repository: NewtonRepository) : ViewModel() {
    private val _uploadState = MutableStateFlow<ScreenState>(ScreenState.Idle)
    val uploadState: StateFlow<ScreenState> get() = _uploadState

    fun uploadProof(token: String, invoiceId: Int, amount: Double, file: File?) {
        if (file == null || !file.exists()) {
            _uploadState.value = ScreenState.Failure("Please select a valid, non-empty proof of payment document.")
            return
        }
        if (amount <= 0.0) {
            _uploadState.value = ScreenState.Failure("Please input a valid transaction amount.")
            return
        }

        _uploadState.value = ScreenState.Loading
        viewModelScope.launch {
            repository.uploadProofOfPayment(token, invoiceId, amount, file)
                .onSuccess { msg -> _uploadState.value = ScreenState.Success(msg) }
                .onFailure { t -> _uploadState.value = ScreenState.Failure(t.message ?: "Network Upload Error") }
        }
    }
}
