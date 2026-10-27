package com.example.nhsentranet.data.repository

import com.example.nhsentranet.network.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

open class NewtonRepository(private val apiService: ApiService) {

    open suspend fun login(username: String, pass: String): Result<UserSessionDto> {
        return try {
            val res = apiService.login(LoginRequest(username, pass))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Login failed"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Network error: Please verify your internet connection."))
        }
    }

    open suspend fun getMarks(token: String, studentId: Int): Result<List<MarkRecordDto>> {
        return try {
            val res = apiService.getMarks("Bearer $token", studentId)
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(res.body()?.message ?: "Unable to fetch academic marks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun uploadProofOfPayment(
        token: String,
        invoiceId: Int,
        amount: Double,
        file: File
    ): Result<String> {
        return try {
            val invBody = invoiceId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val amtBody = amount.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "File",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )

            val res = apiService.uploadProofOfPayment("Bearer $token", invBody, amtBody, filePart)
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()?.message ?: "Upload successful")
            } else {
                Result.failure(Exception(res.body()?.message ?: "Upload rejected by the back-end"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun signConsent(token: String, studentId: Int, consentTypeId: Int, notes: String?): Result<ConsentReceiptDto> {
        return try {
            val res = apiService.signConsent("Bearer $token", SignConsentRequest(studentId, consentTypeId, notes))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Consent signature rejected"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun assignClassTeacher(token: String, staffId: Int, gradeId: Int): Result<String> {
        return try {
            val res = apiService.assignClassTeacher("Bearer $token", ClassTeacherAssignmentDto(staffId, gradeId))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success("Class Teacher assigned successfully.")
            } else {
                Result.failure(Exception(res.body()?.message ?: "Assignment failed on server"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun chatWithAi(token: String, message: String): Result<ChatResponse> {
        return try {
            val res = apiService.sendChatMessage("Bearer $token", ChatRequest(message))
            if (res.isSuccessful && res.body()?.success == true) {
                Result.success(res.body()!!.data!!)
            } else {
                Result.failure(Exception(res.body()?.message ?: "Navigator offline"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
