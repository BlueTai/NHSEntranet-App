package com.example.nhsentranet.network

import com.example.nhsentranet.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

data class LoginRequest(val username: String, val password: String)

data class UserSessionDto(
    val userId: Int,
    val username: String,
    val fullName: String,
    val userRole: String,
    val token: String,
    val linkedChildren: List<ChildDto>?
)

data class ChildDto(
    val studentId: Int,
    val fullName: String,
    val gradeName: String
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

data class CreateStudentWithParentRequest(
    val studentName: String,
    val parentEmail: String
)

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String, val route: String?)

// -------------------------------------------------------------------------
// RETROFIT SERVICE INTERFACE
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
    ): Response<ApiResponse<List<TeacherStudentDto>>>

    @GET("api/Academic/student/{studentId}/report-cards")
    suspend fun getReportCards(
        @Header("Authorization") token: String,
        @Path("studentId") studentId: Int
    ): Response<ApiResponse<List<ReportCardSummaryDto>>>

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
