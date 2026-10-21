package com.example.nhsentranet.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiConfig {
    const val BACKEND_BASE_URL = "https://10.0.2.2:7322"
}

object NewtonApiService {
    var authToken: String? = null

    suspend fun login(username: String, password: String, role: String = ""): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${ApiConfig.BACKEND_BASE_URL}/api/auth/login")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 3000
                readTimeout = 3000
            }

            val payload = JSONObject().apply {
                put("username", username)
                put("password", password)
                if (role.isNotEmpty()) put("role", role)
            }

            conn.outputStream.use { it.write(payload.toString().toByteArray()) }

            if (conn.responseCode in 200..299) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseStr)
                authToken = json.optString("token").takeIf { it.isNotEmpty() }
                return@withContext Result.success(json)
            } else {
                return@withContext Result.failure(Exception("HTTP ${conn.responseCode}"))
            }
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }
    }
}
