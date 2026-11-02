package com.example.nhsentranet.network

import com.example.nhsentranet.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface NewtonAzureApi {
    @GET("api/Dashboard/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStatsDto>>

    @GET("api/Academic/grades")
    suspend fun getGrades(): Response<ApiResponse<List<GradeDto>>>

    @GET("api/People/staff")
    suspend fun getStaff(): Response<ApiResponse<List<StaffDto>>>

    @GET("api/People/students")
    suspend fun getStudents(): Response<ApiResponse<List<StudentDto>>>

    @GET("api/People/contacts")
    suspend fun getContacts(): Response<ApiResponse<List<TeacherContactDto>>>

    @GET("api/Operations/notices")
    suspend fun getNotices(): Response<ApiResponse<List<NoticeDto>>>

    @POST("api/Operations/notices")
    suspend fun createNotice(@Body dto: CreateNoticeDto): Response<ApiResponse<Any>>

    @DELETE("api/Operations/notices/{id}")
    suspend fun deleteNotice(@Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/Operations/events")
    suspend fun getCalendarEvents(): Response<ApiResponse<List<EventDto>>>

    @POST("api/Operations/events")
    suspend fun createCalendarEvent(@Body dto: CreateEventDto): Response<ApiResponse<EventDto>>

    @GET("api/Academic/assessments")
    suspend fun getAssessments(): Response<ApiResponse<List<AssessmentDto>>>

    @GET("api/Academic/student/{studentId}/marks")
    suspend fun getStudentMarks(@Path("studentId") studentId: Int): Response<ApiResponse<List<MarkRecordDto>>>

    @GET("api/Academic/student/{studentId}/subjects")
    suspend fun getStudentSubjects(@Path("studentId") studentId: Int): Response<ApiResponse<List<SubjectSummaryDto>>>

    @GET("api/Academic/student/{studentId}/teachers")
    suspend fun getStudentTeachers(@Path("studentId") studentId: Int): Response<ApiResponse<List<TeacherSummaryDto>>>

    @GET("api/Academic/student/{studentId}/trends")
    suspend fun getStudentTrends(@Path("studentId") studentId: Int): Response<ApiResponse<List<AcademicTrendPointDto>>>

    @GET("api/Academic/student/{studentId}/report-cards")
    suspend fun getStudentReportCards(@Path("studentId") studentId: Int): Response<ApiResponse<List<ReportCardSummaryDto>>>

    @GET("api/Academic/student/{studentId}/comments")
    suspend fun getStudentComments(@Path("studentId") studentId: Int): Response<ApiResponse<List<TeacherCommentSummaryDto>>>

    @GET("api/Academic/student/{studentId}/support")
    suspend fun getStudentSupport(@Path("studentId") studentId: Int): Response<ApiResponse<List<LearningSupportUpdateDto>>>

    @GET("api/Academic/student/{studentId}/assessments")
    suspend fun getStudentAssessments(@Path("studentId") studentId: Int): Response<ApiResponse<List<AssessmentItemDto>>>

    @GET("api/Consent/student/{studentId}")
    suspend fun getStudentConsents(@Path("studentId") studentId: Int): Response<ApiResponse<List<ConsentCurrentDto>>>

    @POST("api/Consent/sign")
    suspend fun signConsent(@Body req: SignConsentRequest): Response<ApiResponse<ConsentReceiptDto>>

    @GET("api/Payments/student/{studentId}/invoices")
    suspend fun getStudentInvoices(@Path("studentId") studentId: Int): Response<ApiResponse<List<InvoiceSummaryDto>>>

    @GET("api/Payments/invoices")
    suspend fun getAllInvoices(): Response<ApiResponse<List<InvoiceSummaryDto>>>

    @GET("api/Operations")
    suspend fun getMedia(): Response<ApiResponse<List<MediaDto>>>

    @GET("api/Operations/locations")
    suspend fun getLocations(): Response<ApiResponse<List<LocationDto>>>

    @POST("api/Admin/students/with-parent")
    suspend fun createStudentWithParent(@Body dto: CreateStudentWithParentDto): Response<ApiResponse<Boolean>>

    @POST("api/Admin/allocations/class-teacher")
    suspend fun assignClassTeacher(@Body dto: ClassTeacherAssignmentDto): Response<ApiResponse<Boolean>>

    @POST("api/Operations/navigator-chat")
    suspend fun sendChatMessage(
        @Header("Authorization") token: String?,
        @Body message: ChatRequest
    ): Response<ApiResponse<ChatResponse>>
}

object AzureApiClient {
    const val WEB_URL = "https://newtonhouse-web-ewb8bwd8awcudvc6.southafricanorth-01.azurewebsites.net/"
    const val BASE_URL = "https://newtonhouse-api-fae2h7d7ezb5arfn.southafricanorth-01.azurewebsites.net/"
    const val API_KEY = "AQ.Ab8RN6LM5qBOAFzcHxo1thleVBYuhDchKoxzEfybE-Y5cfk5VQ"

    private val sharedCookies = mutableMapOf<String, Cookie>()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("X-API-Key", API_KEY)
                .build()
            chain.proceed(request)
        }
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                for (c in cookies) {
                    sharedCookies[c.name] = c
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return sharedCookies.values.toList()
            }
        })
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    val api: NewtonAzureApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewtonAzureApi::class.java)
    }

    fun clearSession() = sharedCookies.clear()

    fun hasAuthCookie(): Boolean = sharedCookies.containsKey("NewtonHouse.Core.Auth")

    suspend fun loginViaWebsiteBridge(username: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            clearSession()

            val getReq = Request.Builder()
                .url("${WEB_URL}Login")
                .header("User-Agent", "NewtonHouseMobile/1.0")
                .get()
                .build()

            val getRes = okHttpClient.newCall(getReq).execute()
            val html = getRes.body?.string() ?: ""

            val tokenRegex = Regex("""name="__RequestVerificationToken"[^>]*value="([^"]+)"""")
            val csrfToken = tokenRegex.find(html)?.groupValues?.get(1) ?: ""

            val formBody = FormBody.Builder()
                .add("Username", username.trim())
                .add("Password", pass)
                .apply {
                    if (csrfToken.isNotEmpty()) {
                        add("__RequestVerificationToken", csrfToken)
                    }
                }
                .build()

            val postReq = Request.Builder()
                .url("${WEB_URL}Login")
                .header("User-Agent", "NewtonHouseMobile/1.0")
                .header("Referer", "${WEB_URL}Login")
                .post(formBody)
                .build()

            okHttpClient.newCall(postReq).execute()

            if (!hasAuthCookie()) {
                return@withContext Result.failure(Exception("Invalid username or password on newtonhouse-web."))
            }

            val statsRes = api.getDashboardStats()
            if (statsRes.isSuccessful && statsRes.body()?.data != null) {
                val stats = statsRes.body()!!.data!!
                val role = stats.userRole ?: UserRoles.Student
                val fullName = stats.fullName ?: username.trim()

                val children = mutableListOf<LinkedChildDto>()
                try {
                    val stuRes = api.getStudents()
                    if (stuRes.isSuccessful && stuRes.body()?.data != null) {
                        val allStudents = stuRes.body()!!.data!!
                        val matching = if (role == UserRoles.Student) {
                            allStudents.filter {
                                it.fullname.equals(fullName, ignoreCase = true) ||
                                it.studentNumber.equals(username.trim(), ignoreCase = true)
                            }
                        } else {
                            allStudents.take(4)
                        }
                        val listToUse = matching.ifEmpty { allStudents.take(2) }
                        listToUse.forEach { s ->
                            children.add(
                                LinkedChildDto(
                                    studentId = s.studentID,
                                    fullName = s.fullname ?: "Student #${s.studentID}",
                                    gradeName = "${s.gradeLevel ?: "Grade 10"} ${s.classSymbol ?: "A"}".trim()
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}

                if (children.isEmpty()) {
                    children.add(LinkedChildDto(1, fullName, "Grade 10 A"))
                }

                return@withContext Result.success(
                    UserSession(
                        userId = 1,
                        username = username.trim(),
                        fullName = fullName,
                        userRole = role,
                        isLiveAzureSession = true,
                        linkedChildren = children
                    )
                )
            } else {
                return@withContext Result.failure(Exception("Authenticated on web, but API returned HTTP ${statsRes.code()}."))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
