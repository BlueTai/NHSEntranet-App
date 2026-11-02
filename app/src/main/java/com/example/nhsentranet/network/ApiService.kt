package com.example.nhsentranet.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

// -------------------------------------------------------------------------
// DATA TRANSFER OBJECTS (DTOs) Map Exactly to swagger.json
// -------------------------------------------------------------------------
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val errors: List<String>?
)

data class LoginRequest(val username: String, val password: String)

data class UserSessionDto(
    val userId: Int,
    val username: String,
    val fullName: String,
    val userRole: String,
    val token: String, // Bearer JWT for security
    val linkedChildren: List<ChildDto>?
)

data class ChildDto(
    val studentId: Int,
    val fullName: String,
    val gradeName: String
)

data class MarkRecordDto(
    val resultID: Int,
    val subject: String,
    val assessmentName: String,
    val assessmentType: String,
    val markObtained: Double,
    val totalMarks: Double,
    val dateRecorded: String
)

data class InvoiceDto(
    val invoiceID: Int,
    val invoiceNumber: String,
    val feeTypeName: String,
    val description: String,
    val amount: Double,
    val amountPaid: Double,
    val balance: Double,
    val dueDate: String,
    val statusName: String
)

data class NoticeDto(
    val noticeID: Int,
    val noticeName: String,
    val noticeType: String,
    val description: String,
    val staffName: String,
    val datePosted: String,
    val isPinned: Boolean
)

data class SignConsentRequest(
    val studentID: Int,
    val consentTypeID: Int,
    val notes: String?
)

data class ConsentReceiptDto(
    val consentID: Int,
    val consentTypeName: String,
    val signedAt: String,
    val ipAddress: String?,
    val studentName: String,
    val parentName: String
)

data class ClassTeacherAssignmentDto(
    val staffID: Int,
    val gradeID: Int
)

data class CreateStudentWithParentRequest(
    val studentName: String,
    val parentEmail: String
)

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String, val route: String?)

// -------------------------------------------------------------------------
// RETROFIT SERVICE INTERFACE (Zero direct DB, JWT authorized)
// -------------------------------------------------------------------------
interface ApiService {
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserSessionDto>>

    @GET("api/Academic/student/{studentId}/marks")
    suspend fun getMarks(
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<ApiResponse<List<MarkRecordDto>>>

    @GET("api/Academic/teacher/{teacherId}/students")
    suspend fun getTeacherStudents(
        @Header("Authorization") token: String,
        @Path("teacherId") teacherId: Int
    ): Response<ApiResponse<List<com.example.nhsentranet.model.TeacherStudentDto>>>

    @GET("api/Academic/student/{studentId}/report-cards")
    suspend fun getReportCards(
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<ApiResponse<List<com.example.nhsentranet.model.ReportCardSummaryDto>>>

    @GET("api/Operations/notices")
    suspend fun getNotices(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<NoticeDto>>>

    @DELETE("api/Operations/notices/{noticeId}")
    suspend fun deleteNotice(
        @Header("Authorization") token: String,
        @Path("noticeId") noticeId: Int
    ): Response<ApiResponse<Boolean>>

    @GET("api/Payments/student/{studentId}/invoices")
    suspend fun getInvoices(
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<ApiResponse<List<InvoiceDto>>>

    @Multipart
    @POST("api/Payments/upload")
    suspend fun uploadProofOfPayment(
        @Header("Authorization") token: String,
        @Part("InvoiceID") invoiceId: RequestBody,
        @Part("Amount") amount: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<String>>

    @POST("api/Consent/sign")
    suspend fun signConsent(
        @Header("Authorization") token: String,
        @Body request: SignConsentRequest
    ): Response<ApiResponse<ConsentReceiptDto>>

    @POST("api/Admin/allocations/class-teacher")
    suspend fun assignClassTeacher(
        @Header("Authorization") token: String,
        @Body request: ClassTeacherAssignmentDto
    ): Response<ApiResponse<Boolean>>

    @POST("api/Admin/students/with-parent")
    suspend fun createStudentWithParent(
        @Header("Authorization") token: String,
        @Body request: CreateStudentWithParentRequest
    ): Response<ApiResponse<Boolean>>

    @POST("api/Operations/navigator-chat")
    suspend fun sendChatMessage(
        @Header("Authorization") token: String,
        @Body message: ChatRequest
    ): Response<ApiResponse<ChatResponse>>
}
