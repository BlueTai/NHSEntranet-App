package com.example.nhsentranet.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// =========================================================================
// 3. FIREBASE CLOUD FIRESTORE DATABASE & SWAGGER SYNC
// =========================================================================
object NewtonFirebaseDatabase {
    private val db = FirebaseFirestore.getInstance()

    // Fallback accounts if Firebase is empty
    private val defaultAccounts = mapOf(
        "admin" to mapOf("userId" to 1, "username" to "admin", "password" to "Admin@123", "fullName" to "System Administrator", "role" to UserRoles.Admin),
        "jsmith" to mapOf("userId" to 2, "username" to "jsmith", "password" to "Teacher@123", "fullName" to "Jane Smith", "role" to UserRoles.Teacher),
        "john.johnson" to mapOf("userId" to 3, "username" to "john.johnson", "password" to "Student@123", "fullName" to "John Johnson", "role" to UserRoles.Student),
        "sjohnson" to mapOf("userId" to 4, "username" to "sjohnson", "password" to "Parent@123", "fullName" to "Sarah Johnson", "role" to UserRoles.Parent),
        "hod" to mapOf("userId" to 5, "username" to "hod", "password" to "Hod@123", "fullName" to "Dr. Adams", "role" to UserRoles.HeadOfDepartment)
    )

    suspend fun login(username: String, password: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val cleanUser = username.trim().lowercase()

        try {
            val adminDoc = db.collection("users").document("admin").get().await()
            if (!adminDoc.exists()) {
                defaultAccounts.forEach { (user, data) ->
                    db.collection("users").document(user).set(data).await()
                }
            }

            val snapshot = db.collection("users").document(cleanUser).get().await()
            if (snapshot.exists() && snapshot.getString("password") == password) {
                val role = snapshot.getString("role") ?: UserRoles.Student
                val children = if (role == UserRoles.Parent || role == UserRoles.Student) {
                    listOf(
                        LinkedChildDto(1, "John Johnson", "Grade 10A"),
                        LinkedChildDto(2, "Emily Johnson", "Grade 8B")
                    )
                } else emptyList()

                return@withContext Result.success(
                    UserSession(
                        userId = snapshot.getLong("userId")?.toInt() ?: 1,
                        username = cleanUser,
                        fullName = snapshot.getString("fullName") ?: cleanUser,
                        role = role,
                        linkedChildren = children
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext Result.failure(Exception("Invalid username or password."))
    }

    suspend fun getDashboardStats(session: UserSession): DashboardStatsModel = withContext(Dispatchers.IO) {
        return@withContext DashboardStatsModel(
            fullName = session.fullName,
            userRole = session.userRole,
            totalUsers = 48, students = 30, parents = 25, pendingApprovals = 3,
            teachersCount = 10, attendanceRate = 96.4, classesToday = 5, pendingMarking = 3,
            upcomingAssessments = listOf("Mathematics: Term Test 1", "Physical Sciences: Lab")
        )
    }

    suspend fun uploadStudentMark(studentId: Int = 1, mark: Double, isHod: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docId = "mark_${studentId}_${System.currentTimeMillis()}"
            val markData = hashMapOf(
                "resultId" to (10..999).random(),
                "studentId" to studentId,
                "subject" to "Information Technology",
                "assessmentName" to "Recent Assessment",
                "assessmentType" to "Test",
                "markObtained" to mark,
                "totalMarks" to 100.0,
                "isApproved" to isHod, // Requires HOD approval unless uploaded by Head of Academics
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("marks").document(docId).set(markData).await()
            val status = if (isHod) "Mark ($mark%) approved & published live." else "Mark ($mark%) submitted for HOD moderation."
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to upload mark: ${e.message}"))
        }
    }

    suspend fun approveMark(docId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("marks").document(docId).update("isApproved", true).await()
            Result.success("Mark approved successfully and published to parents.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNotice(title: String, desc: String, author: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val noticeData = hashMapOf(
                "noticeName" to title,
                "noticeType" to "General",
                "description" to desc,
                "staffName" to author,
                "datePosted" to "Just now",
                "isPinned" to false,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("notices").add(noticeData).await()
            Result.success("Notice posted to Firebase.")
        } catch (e: Exception) {
            Result.failure(Exception("Failed to post notice: ${e.message}"))
        }
    }

    suspend fun submitForm(
        studentName: String,
        formType: String,
        parentOccupation: String,
        allergies: String,
        driverInfo: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docId = "form_${System.currentTimeMillis()}"
            val formData = hashMapOf(
                "formId" to docId,
                "studentName" to studentName,
                "formType" to formType,
                "parentOccupation" to parentOccupation,
                "allergies" to allergies,
                "driverInfo" to driverInfo,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("forms").document(docId).set(formData).await()
            Result.success("Form saved to Firebase & ready for PDF download.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotices(): List<NoticeModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("notices").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val list = mutableListOf<NoticeModel>()
            var idCounter = 1
            for (doc in snapshot.documents) {
                list.add(
                    NoticeModel(
                        noticeId = idCounter++,
                        noticeName = doc.getString("noticeName") ?: "",
                        noticeType = doc.getString("noticeType") ?: "General",
                        description = doc.getString("description") ?: "",
                        staffName = doc.getString("staffName") ?: "",
                        datePosted = doc.getString("datePosted") ?: "Today",
                        isPinned = doc.getBoolean("isPinned") ?: false
                    )
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listOf(
            NoticeModel(1, "Term 4 Examination Timetable", "Academic", "Final examination schedules have been published.", "Jane Smith", "Today", true),
            NoticeModel(2, "POPIA & Medical Consent Reminder", "Urgent", "Please verify annual medical consents.", "System Administrator", "Yesterday", true)
        )
    }

    suspend fun getMarks(studentId: Int = 1): List<MarkRecordModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = db.collection("marks").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val list = mutableListOf<MarkRecordModel>()
            var idCounter = 1
            for (doc in snapshot.documents) {
                list.add(
                    MarkRecordModel(
                        resultId = idCounter++,
                        subject = doc.getString("subject") ?: "",
                        assessmentName = doc.getString("assessmentName") ?: "",
                        assessmentType = doc.getString("assessmentType") ?: "Test",
                        markObtained = doc.getDouble("markObtained") ?: 0.0,
                        totalMarks = doc.getDouble("totalMarks") ?: 100.0,
                        isApproved = doc.getBoolean("isApproved") ?: true
                    )
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (e: Exception) {
            e.printStackTrace()
        }

        listOf(
            MarkRecordModel(1, "Mathematics", "Term 1 Test", "Test", 85.0, 100.0, true),
            MarkRecordModel(2, "Physical Sciences", "Practical Investigation", "Practical", 78.0, 100.0, true)
        )
    }
}
