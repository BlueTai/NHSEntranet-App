package com.example.nhsentranet.model

import androidx.compose.ui.graphics.vector.ImageVector

object UserRoles {
    const val Admin = "Admin"
    const val Teacher = "Teacher"
    const val HeadOfDepartment = "HeadOfDepartment" // Head of Academics
    const val Parent = "Parent"
    const val Student = "Student"
}

data class LinkedChildDto(
    val studentId: Int,
    val fullName: String,
    val gradeName: String
)

data class UserSession(
    val userId: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val linkedChildren: List<LinkedChildDto> = emptyList()
)

data class MarkRecordModel(
    val resultId: Int,
    val subject: String,
    val assessmentName: String,
    val assessmentType: String,
    val markObtained: Double,
    val totalMarks: Double,
    val isApproved: Boolean
)

data class NoticeModel(
    val noticeId: Int,
    val noticeName: String,
    val noticeType: String,
    val description: String,
    val staffName: String,
    val datePosted: String,
    val isPinned: Boolean
)

data class InvoiceSummaryModel(
    val invoiceId: Int,
    val invoiceNumber: String,
    val feeTypeName: String,
    val description: String,
    val amount: Double,
    val amountPaid: Double,
    val balance: Double,
    val dueDate: String,
    val statusName: String
)

data class TransportRouteDto(
    val routeName: String,
    val driverName: String,
    val driverPhone: String,
    val eta: String,
    val status: String
)

data class IncidentDto(
    val incidentId: Int,
    val studentName: String,
    val category: String,
    val description: String,
    val severity: Int,
    val status: String,
    val occurredAt: String
)

data class MediaDto(
    val id: Int,
    val title: String,
    val type: String,
    val url: String
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val suggestedRoute: String? = null,
    val actionLabel: String? = null
)

data class PortalTile(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val allowedRoles: List<String>
)

data class RoleSectionView(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null
)
