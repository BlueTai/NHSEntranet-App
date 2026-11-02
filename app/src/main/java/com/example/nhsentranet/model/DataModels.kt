package com.example.nhsentranet.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

object UserRoles {
    const val Admin = "Admin"
    const val Teacher = "Teacher"
    const val HeadOfDepartment = "HeadOfDepartment"
    const val Parent = "Parent"
    const val Student = "Student"
}

// Generic API response container
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val errors: List<String>? = null
)

// Auth & Session DTOs
data class MobileLoginDto(val username: String, val password: String)

data class LinkedChildDto(
    val studentId: Int,
    val fullName: String,
    val gradeName: String
)

data class UserSession(
    val userId: Int = 0,
    val username: String = "",
    val fullName: String = "",
    val userRole: String = "",
    val token: String? = null,
    val isLiveAzureSession: Boolean = true,
    val linkedChildren: List<LinkedChildDto> = emptyList()
) {
    val role: String get() = userRole

    constructor(
        userId: Int,
        username: String,
        fullName: String,
        userRole: String,
        linkedChildren: List<LinkedChildDto>
    ) : this(
        userId = userId,
        username = username,
        fullName = fullName,
        userRole = userRole,
        token = null,
        isLiveAzureSession = true,
        linkedChildren = linkedChildren
    )

    constructor(
        userId: Int,
        username: String,
        fullName: String,
        userRole: String,
        isLiveAzureSession: Boolean,
        linkedChildren: List<LinkedChildDto> = emptyList()
    ) : this(
        userId = userId,
        username = username,
        fullName = fullName,
        userRole = userRole,
        token = null,
        isLiveAzureSession = isLiveAzureSession,
        linkedChildren = linkedChildren
    )

    constructor(
        userId: Int,
        username: String,
        fullName: String,
        role: String,
        token: String? = null,
        linkedChildren: List<LinkedChildDto> = emptyList()
    ) : this(
        userId = userId,
        username = username,
        fullName = fullName,
        userRole = role,
        token = token,
        isLiveAzureSession = true,
        linkedChildren = linkedChildren
    )
}

// Dashboard DTOs
data class StaffStatsDto(val teachers: Int = 0, val support: Int = 0, val leadership: Int = 0)
data class UpcomingAssessmentDto(val name: String?, val subject: String?, val dueDate: String?)
data class DashboardNoticeDto(val title: String?, val date: String?)
data class DashboardTimetableDto(val time: String? = null, val subject: String?, val teacher: String?)
data class DashboardEventDto(val title: String?, val date: String?)

data class DashboardStatsDto(
    val fullName: String?,
    val userRole: String?,
    val totalUsers: Int = 0,
    val students: Int = 0,
    val parents: Int = 0,
    val pendingApprovals: Int = 0,
    val staff: StaffStatsDto? = null,
    val attendanceRate: Double = 0.0,
    val upcomingAssessments: List<UpcomingAssessmentDto>? = emptyList(),
    val notices: List<DashboardNoticeDto>? = emptyList(),
    val timetable: List<DashboardTimetableDto>? = emptyList(),
    val events: List<DashboardEventDto>? = emptyList(),
    val classesToday: Int = 0,
    val pendingMarking: Int = 0
)

data class ClassDto(
    val classID: Int,
    val gradeID: Int,
    val gradeLevel: String?,
    val classSymbol: String?,
    val classTeacherStaffID: Int?,
    val classTeacherName: String?,
    val studentCount: Int = 0
)

data class GradeDto(
    val gradeID: Int,
    val gradeLevel: String,
    val studentCount: Int = 0,
    val classes: List<ClassDto>? = emptyList()
)

data class ClassTeacherAssignmentDto(
    val classID: Int,
    val staffID: Int
)

// People DTOs
data class StaffDto(
    val staffID: Int,
    val userID: Int = 0,
    val firstname: String? = null,
    val lastname: String? = null,
    val fullname: String?,
    val email: String?,
    val userRole: String?,
    val contactNumber: String?,
    val isActive: Boolean = true
)

data class StudentAddressDto(
    val addressID: Int = 0,
    val parentID: Int = 0,
    val addressType: String?,
    val line1: String?,
    val suburb: String?,
    val city: String?,
    val province: String?,
    val postalCode: String?
)

data class StudentDto(
    val studentID: Int,
    val studentNumber: String? = null,
    val studentNum: String? = studentNumber,
    val userID: Int = 0,
    val fullname: String?,
    val email: String? = null,
    val classID: Int = 0,
    val classSymbol: String?,
    val gradeID: Int = 0,
    val gradeLevel: String?,
    val classTeacher: String?,
    val medicalAlert: String? = null,
    val emergencyName: String? = null,
    val emergencyPhone: String? = null,
    val wellnessFlag: Boolean = false,
    val addresses: List<StudentAddressDto>? = emptyList()
)

data class TeacherContactDto(
    val staffID: Int,
    val fullname: String,
    val userRole: String?,
    val email: String?,
    val contactNumber: String?,
    val classTeacherOf: List<String>? = emptyList(),
    val subjectsTaught: List<String>? = emptyList()
)

data class CreateStudentWithParentDto(
    val firstname: String,
    val lastname: String,
    val email: String,
    val password: String = "Student@123",
    val dob: String = "2010-05-12",
    val enrolmentDate: String = "2026-01-15",
    val classID: Int = 1,
    val gradeID: Int? = 1,
    val parentFirstname: String,
    val parentLastname: String,
    val parentEmail: String,
    val parentPassword: String = "Parent@123",
    val parentPhone: String,
    val parentRelationship: String = "Guardian",
    val parentAddressLine1: String = "123 Newton Road",
    val parentAddressSuburb: String = "Randburg",
    val parentAddressCity: String = "Johannesburg",
    val parentAddressProvince: String = "Gauteng",
    val parentAddressPostalCode: String = "2194"
)

// Operations & Calendar DTOs
data class NoticeDto(
    val noticeID: Int,
    val noticeName: String,
    val noticeType: String,
    val description: String?,
    val staffID: Int? = null,
    val staffName: String?,
    val gradeID: Int? = null,
    @SerializedName("gradeLevel", alternate = ["gradeName"])
    val gradeLevel: String? = null,
    val datePosted: String,
    val isPinned: Boolean
)

data class CreateNoticeDto(
    val noticeName: String,
    val noticeType: String = "General",
    val description: String,
    val gradeID: Int? = null,
    val isPinned: Boolean = false
)

data class EventDto(
    val eventID: Int = 0,
    val title: String,
    val subtitle: String?,
    val eventType: String?,
    val color: String? = null,
    val eventDate: String,
    val location: String?,
    val isPublic: Boolean = true,
    val createdByStaffID: Int? = null,
    val createdByStaffName: String? = null
)

data class CreateEventDto(
    val title: String,
    val subtitle: String? = null,
    val eventType: String = "General",
    val color: String = "teal",
    val eventDate: String = "2026-11-15T09:00:00Z",
    val location: String? = "School Hall",
    val isPublic: Boolean = true
)

data class CalendarEventDto(
    val eventID: Int = 0,
    val eventDate: String,
    val title: String,
    val eventType: String,
    val subtitle: String?,
    val location: String?,
    val isPublic: Boolean = true
)

// Academic Centre DTOs
data class MarkRecordDto(
    val resultID: Int,
    val subject: String,
    val assessmentName: String,
    val assessmentType: String,
    val markObtained: Double,
    val totalMarks: Double,
    val dateRecorded: String? = null
)

data class AssessmentItemDto(
    val assessmentID: Int,
    @SerializedName("title", alternate = ["assessmentName"])
    val title: String,
    val subject: String?,
    val dueDate: String?,
    @SerializedName("totalMark", alternate = ["totalMarks"])
    val totalMark: Double?,
    val type: String?
)

data class SubjectSummaryDto(val subjectID: Int, val subjectName: String, val description: String?, val teacherName: String?, val currentMark: Double)
data class TeacherSummaryDto(val staffID: Int, val fullName: String, val subject: String?, val email: String?, val phone: String?)
data class AcademicTrendPointDto(val term: String, val overallAverage: Double)

data class TeacherCommentSummaryDto(
    val commentID: Int,
    @SerializedName("comment", alternate = ["commentText"])
    val comment: String,
    val commentDate: String,
    val staffName: String?
)

data class LearningSupportUpdateDto(
    val updateID: Int,
    val studentID: Int,
    val title: String?,
    val categoryID: Int = 0,
    val categoryName: String?,
    val update: String?,
    val nextSteps: String? = null,
    val supportStaff: String?,
    val datePosted: String?
)

data class AssessmentDto(
    val assessmentID: Int,
    @SerializedName("title", alternate = ["assessmentName"])
    val title: String,
    val assessmentTypeCode: String?,
    val assessmentStatusCode: String?,
    val subjectName: String?,
    @SerializedName("gradeLevel", alternate = ["gradeName"])
    val gradeLevel: String?,
    val dueDate: String?,
    @SerializedName("totalMark", alternate = ["totalMarks"])
    val totalMark: Double?
)

// Consent / Forms DTOs
data class ConsentCurrentDto(
    val studentID: Int,
    val consentTypeID: Int,
    val consentCode: String,
    val consentName: String,
    val isRequired: Boolean,
    val isSigned: Boolean
)

data class SignConsentRequest(
    val studentID: Int,
    val consentTypeID: Int,
    val notes: String?
)

data class ConsentReceiptDto(
    val consentID: Int,
    val consentTypeName: String?,
    val signedAt: String?
)

// Payments DTOs
data class InvoiceSummaryDto(
    val invoiceID: Int,
    val invoiceNumber: String?,
    val studentName: String?,
    val feeTypeName: String?,
    val amount: Double,
    val amountPaid: Double,
    val balance: Double,
    val statusName: String?,
    val dueDate: String?
)

// Operations Media & Location DTOs
data class MediaDto(
    val mediaID: Int = 0,
    val fileURL: String = "",
    val mediaType: String = "",
    val description: String? = null,
    val gradeID: Int? = null,
    @SerializedName("gradeLevel", alternate = ["gradeName"])
    val gradeLevel: String? = null,
    val uploadedByStaffName: String? = null,
    val eventDate: String? = null,
    val id: Int = mediaID,
    val title: String = description ?: "",
    val type: String = mediaType,
    val url: String = fileURL
)

data class LocationDto(
    val locationID: Int,
    @SerializedName("name", alternate = ["locationName"])
    val name: String,
    val description: String?,
    val mapURL: String?,
    val locationType: String? = "CampusPoint"
)

// Navigation DTO
data class SidebarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val roles: List<String>
)

// Legacy wrappers for backward compatibility
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

data class TeacherStudentDto(
    val subjectID: Int,
    val subjectName: String,
    val studentID: Int,
    val studentName: String,
    val gradeName: String,
    val currentMark: Double
)

data class ReportCardSummaryDto(
    val reportCardID: Int = 0,
    val studentID: Int = 0,
    val term: String = "",
    val academicYear: Int = 2026,
    @SerializedName("gradeLevel", alternate = ["gradeName"])
    val gradeLevel: String? = null,
    val overallAverage: Double? = null,
    val statusCode: String? = null
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
