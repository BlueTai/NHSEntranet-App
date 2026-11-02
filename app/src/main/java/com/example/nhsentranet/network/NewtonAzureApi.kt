package com.example.nhsentranet.network

import com.example.nhsentranet.model.AcademicTrendPointDto
import com.example.nhsentranet.model.ApiResponse
import com.example.nhsentranet.model.AssessmentDto
import com.example.nhsentranet.model.AssessmentItemDto
import com.example.nhsentranet.model.CalendarEventDto
import com.example.nhsentranet.model.ConsentCurrentDto
import com.example.nhsentranet.model.ConsentReceiptDto
import com.example.nhsentranet.model.CreateNoticeDto
import com.example.nhsentranet.model.CreateStudentWithParentDto
import com.example.nhsentranet.model.DashboardStatsDto
import com.example.nhsentranet.model.InvoiceSummaryDto
import com.example.nhsentranet.model.LearningSupportUpdateDto
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.LocationDto
import com.example.nhsentranet.model.MarkRecordDto
import com.example.nhsentranet.model.MediaDto
import com.example.nhsentranet.model.MobileLoginDto
import com.example.nhsentranet.model.NoticeDto
import com.example.nhsentranet.model.ReportCardSummaryDto
import com.example.nhsentranet.model.SignConsentRequest
import com.example.nhsentranet.model.StaffDto
import com.example.nhsentranet.model.StudentDto
import com.example.nhsentranet.model.SubjectSummaryDto
import com.example.nhsentranet.model.TeacherCommentSummaryDto
import com.example.nhsentranet.model.TeacherContactDto
import com.example.nhsentranet.model.TeacherSummaryDto
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface NewtonAzureApi {
    @POST("api/Auth/login")
    suspend fun login(@Body dto: MobileLoginDto): Response<ApiResponse<UserSession>>

    @GET("api/Dashboard/stats")
    suspend fun getDashboardStats(): Response<ApiResponse<DashboardStatsDto>>

    @GET("api/People/staff")
    suspend fun getStaff(): Response<ApiResponse<List<StaffDto>>>

    @GET("api/People/students")
    suspend fun getStudents(): Response<ApiResponse<List<StudentDto>>>

    @GET("api/People/students/by-user/{userId}")
    suspend fun getStudentIdByUserId(@Path("userId") userId: Int): Response<ApiResponse<Int?>>

    @GET("api/People/students/by-parent-user/{userId}")
    suspend fun getFirstChildIdByParentUserId(@Path("userId") userId: Int): Response<ApiResponse<Int?>>

    @GET("api/People/contacts")
    suspend fun getContacts(): Response<ApiResponse<List<TeacherContactDto>>>

    @GET("api/Operations/notices")
    suspend fun getNotices(): Response<ApiResponse<List<NoticeDto>>>

    @POST("api/Operations/notices")
    suspend fun createNotice(@Body dto: CreateNoticeDto): Response<ApiResponse<Any>>

    @DELETE("api/Operations/notices/{id}")
    suspend fun deleteNotice(@Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/Operations/events")
    suspend fun getCalendarEvents(): Response<ApiResponse<List<CalendarEventDto>>>

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
}

object AzureApiClient {
    const val WEB_URL = "https://newtonhouse-web-ewb8bwd8awcudvc6.southafricanorth-01.azurewebsites.net/"
    const val BASE_URL = "https://newtonhouse-api-fae2h7d7ezb5arfn.southafricanorth-01.azurewebsites.net/"

    private val sharedCookies = mutableMapOf<String, Cookie>()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                for (c in cookies) {
                    sharedCookies[c.name] = c
                }
            }

            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                val list = mutableListOf<Cookie>()
                for (c in sharedCookies.values) {
                    try {
                        val remapped = Cookie.Builder()
                            .name(c.name)
                            .value(c.value)
                            .domain(url.host)
                            .path("/")
                            .build()
                        list.add(remapped)
                    } catch (_: Exception) {
                        list.add(c)
                    }
                }
                return list
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

    fun hasAuthCookie(): Boolean = sharedCookies.containsKey("NewtonHouse.Core.Auth") || sharedCookies.keys.any { it.contains("Auth") || it.contains("Identity") || it.contains("Session") }

    suspend fun loginViaWebsiteBridge(username: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            clearSession()

            // 1. Try Direct API Login
            try {
                val apiRes = api.login(MobileLoginDto(username.trim(), pass))
                if (apiRes.isSuccessful && apiRes.body()?.success == true && apiRes.body()?.data != null) {
                    return@withContext Result.success(apiRes.body()!!.data!!)
                }
            } catch (_: Exception) {}

            // 2. Try Web Bridge Login across common MVC/Identity endpoints
            val loginPaths = listOf("Login", "Account/Login", "Identity/Account/Login")

            for (path in loginPaths) {
                try {
                    val getReq = okhttp3.Request.Builder()
                        .url("$WEB_URL$path")
                        .header("User-Agent", "NewtonHouseMobile/1.0")
                        .get()
                        .build()

                    val getRes = okHttpClient.newCall(getReq).execute()
                    val html = getRes.body?.string() ?: ""

                    val tokenRegex = Regex("""name="__RequestVerificationToken"[^>]*value="([^"]+)"""")
                    val csrfToken = tokenRegex.find(html)?.groupValues?.get(1) ?: ""

                    val formBody = okhttp3.FormBody.Builder()
                        .add("Username", username.trim())
                        .add("Input.Username", username.trim())
                        .add("Email", username.trim())
                        .add("Input.Email", username.trim())
                        .add("Password", pass)
                        .add("Input.Password", pass)
                        .apply {
                            if (csrfToken.isNotEmpty()) {
                                add("__RequestVerificationToken", csrfToken)
                            }
                        }
                        .build()

                    val postReq = okhttp3.Request.Builder()
                        .url("$WEB_URL$path")
                        .header("User-Agent", "NewtonHouseMobile/1.0")
                        .header("Referer", "$WEB_URL$path")
                        .post(formBody)
                        .build()

                    val postRes = okHttpClient.newCall(postReq).execute()
                    if (hasAuthCookie() || postRes.isSuccessful) {
                        break
                    }
                } catch (_: Exception) {}
            }

            // 3. Verify if Auth Cookie exists or query Dashboard stats
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
                                (it.fullname ?: "").equals(fullName, ignoreCase = true) ||
                                (it.studentNumber ?: "").equals(username.trim(), ignoreCase = true)
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
                                    gradeName = "Grade ${s.gradeLevel ?: "10"}${s.classSymbol ?: "A"}"
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
            }

            if (!hasAuthCookie()) {
                return@withContext Result.failure(Exception("Invalid username or password."))
            }

            return@withContext Result.failure(Exception("Authentication succeeded, but failed to retrieve user session details."))
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
