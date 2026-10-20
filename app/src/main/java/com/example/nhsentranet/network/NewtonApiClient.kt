package com.example.nhsentranet.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class UserSession(
    val userId: Int = 0,
    val username: String,
    val fullName: String = "",
    val role: String,
    val token: String? = null
)

object NewtonApiClient {

    // IMPORTANT: Change 7322 to match the exact port in your website's launchSettings.json
    // Or replace with your live Azure URL (e.g., "https://newtonhouse.azurewebsites.net")
    const val BASE_URL = "https://10.0.2.2:7322"

    var currentSession: UserSession? = null
    private var authCookie: String? = null

    init {
        // Automatically store and forward ASP.NET Core session/authentication cookies
        CookieHandler.setDefault(CookieManager(null, CookiePolicy.ACCEPT_ALL))
        trustLocalDevCertificates()
    }

    /**
     * Connects to the website's /api/auth/login endpoint and authenticates the user.
     */
    suspend fun login(username: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/auth/login")
            val conn = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 6000
                readTimeout = 6000
                doOutput = true
            }

            // Build JSON payload matching the website's login model
            val payload = JSONObject().apply {
                put("username", username.trim())
                put("password", password)
            }

            conn.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                // Capture ASP.NET Core auth cookie for future API calls
                conn.headerFields["Set-Cookie"]?.let { cookies ->
                    authCookie = cookies.joinToString("; ")
                }

                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)

                val session = UserSession(
                    userId = json.optInt("userId", 0),
                    username = json.optString("username", username),
                    fullName = json.optString("fullName", username),
                    role = json.optString("role", "Student"),
                    token = json.optString("token").takeIf { it.isNotEmpty() }
                )

                currentSession = session
                return@withContext Result.success(session)
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return@withContext Result.failure(Exception("Invalid username or password."))
            } else {
                return@withContext Result.failure(Exception("Server error (HTTP $responseCode)."))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Cannot reach server at $BASE_URL. Check if the website is running."))
        }
    }

    /**
     * Helper for making authenticated GET/POST calls to other website API endpoints
     */
    fun createAuthenticatedConnection(endpoint: String, method: String = "GET"): HttpsURLConnection {
        val url = URL("$BASE_URL$endpoint")
        return (url.openConnection() as HttpsURLConnection).apply {
            requestMethod = method
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            authCookie?.let { setRequestProperty("Cookie", it) }
            currentSession?.token?.let { setRequestProperty("Authorization", "Bearer $it") }
            connectTimeout = 6000
            readTimeout = 6000
        }
    }

    /**
     * Bypasses Android's SSL block for ASP.NET Core's self-signed localhost certificate
     */
    private fun trustLocalDevCertificates() {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
            })
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
