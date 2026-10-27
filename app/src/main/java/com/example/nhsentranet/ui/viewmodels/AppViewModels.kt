package com.example.nhsentranet.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhsentranet.data.NewtonFirebaseRepository
import com.example.nhsentranet.model.InvoiceSummaryModel
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.MarkRecordModel
import com.example.nhsentranet.model.NoticeModel
import com.example.nhsentranet.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =========================================================================
// 1. AUTH VIEWMODEL
// =========================================================================
class AuthViewModel(
    private val repository: NewtonFirebaseRepository = NewtonFirebaseRepository.getInstance()
) : ViewModel() {

    private val _session = MutableStateFlow<UserSession?>(null)
    val session: StateFlow<UserSession?> = _session.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.autoSeedSwaggerData()
        }
    }

    fun login(username: String, pass: String) {
        if (username.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Enter username and password."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val res = repository.login(username, pass)
            _isLoading.value = false
            res.onSuccess {
                _session.value = it
            }.onFailure {
                _errorMessage.value = it.message ?: "Authentication failed."
            }
        }
    }

    fun logout() {
        _session.value = null
        _errorMessage.value = null
    }
}

// =========================================================================
// 2. DASHBOARD VIEWMODEL
// =========================================================================
class DashboardViewModel : ViewModel() {
    private val _selectedChild = MutableStateFlow<LinkedChildDto?>(null)
    val selectedChild: StateFlow<LinkedChildDto?> = _selectedChild.asStateFlow()

    fun initChild(initialChild: LinkedChildDto?) {
        if (_selectedChild.value == null) {
            _selectedChild.value = initialChild
        }
    }

    fun selectChild(child: LinkedChildDto) {
        _selectedChild.value = child
    }
}

// =========================================================================
// 3. ACADEMIC VIEWMODEL
// =========================================================================
class AcademicViewModel(
    private val repository: NewtonFirebaseRepository = NewtonFirebaseRepository.getInstance()
) : ViewModel() {

    private val _marks = MutableStateFlow<List<MarkRecordModel>>(emptyList())
    val marks: StateFlow<List<MarkRecordModel>> = _marks.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun loadMarks(studentId: Int, isParentOrStudent: Boolean) {
        viewModelScope.launch {
            _marks.value = repository.getMarks(studentId, isParentOrStudent)
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
}

// =========================================================================
// 4. NOTICES VIEWMODEL
// =========================================================================
class NoticesViewModel(
    private val repository: NewtonFirebaseRepository = NewtonFirebaseRepository.getInstance()
) : ViewModel() {

    private val _notices = MutableStateFlow<List<NoticeModel>>(emptyList())
    val notices: StateFlow<List<NoticeModel>> = _notices.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    private val _postFeedback = MutableStateFlow<String?>(null)
    val postFeedback: StateFlow<String?> = _postFeedback.asStateFlow()

    fun loadNotices() {
        viewModelScope.launch {
            _isLoading.value = true
            _notices.value = repository.getNotices()
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun postNotice(title: String, desc: String, author: String) {
        if (title.isBlank()) return
        _isPosting.value = true
        viewModelScope.launch {
            val res = repository.postNotice(title, desc, author)
            _isPosting.value = false
            res.onSuccess {
                _postFeedback.value = "Notice published live to Firebase!"
                _notices.value = repository.getNotices()
            }.onFailure {
                _postFeedback.value = it.message
            }
        }
    }

    fun clearFeedback() {
        _postFeedback.value = null
    }
}

// =========================================================================
// 5. INVOICES VIEWMODEL
// =========================================================================
class InvoicesViewModel(
    private val repository: NewtonFirebaseRepository = NewtonFirebaseRepository.getInstance()
) : ViewModel() {

    private val _invoices = MutableStateFlow<List<InvoiceSummaryModel>>(emptyList())
    val invoices: StateFlow<List<InvoiceSummaryModel>> = _invoices.asStateFlow()

    private val _proofMsg = MutableStateFlow<String?>(null)
    val proofMsg: StateFlow<String?> = _proofMsg.asStateFlow()

    fun loadInvoices(studentId: Int) {
        viewModelScope.launch {
            _invoices.value = repository.getInvoices(studentId)
        }
    }

    fun uploadProofOfPayment(invoiceId: Int) {
        viewModelScope.launch {
            val res = repository.uploadProofOfPayment(invoiceId)
            _proofMsg.value = res.getOrElse { it.message ?: "Upload failed" }
        }
    }
}
