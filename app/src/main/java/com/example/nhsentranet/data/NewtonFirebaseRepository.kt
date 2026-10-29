package com.example.nhsentranet.data

import com.example.nhsentranet.model.IncidentDto
import com.example.nhsentranet.model.InvoiceSummaryModel
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.MarkRecordModel
import com.example.nhsentranet.model.NoticeModel
import com.example.nhsentranet.model.TransportRouteDto
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// =========================================================================
// DATA REPOSITORY - FIREBASE CLOUD FIRESTORE PERSISTENCE LAYER
// =========================================================================
class NewtonFirebaseRepository private constructor() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val demoUsers = mapOf(
        "admin" to mapOf("userId" to 1, "username" to "admin", "password" to "Admin@123", "fullName" to "System Administrator", "userRole" to UserRoles.Admin),
        "jsmith" to mapOf("userId" to 2, "username" to "jsmith", "password" to "Teacher@123", "fullName" to "Jane Smith", "userRole" to UserRoles.Teacher),
        "john.johnson" to mapOf("userId" to 3, "username" to "john.johnson", "password" to "Student@123", "fullName" to "John Johnson", "userRole" to UserRoles.Student),
        "sjohnson" to mapOf("userId" to 4, "username" to "sjohnson", "password" to "Parent@123", "fullName" to "Sarah Johnson", "userRole" to UserRoles.Parent),
        "hod" to mapOf("userId" to 5, "username" to "hod", "password" to "Hod@123", "fullName" to "Dr. Adams", "userRole" to UserRoles.HeadOfDepartment)
    )

    suspend fun autoSeedSwaggerData() = withContext(Dispatchers.IO) {
        try {
            val adminDoc = db.collection("users").document("admin").get().await()
            if (!adminDoc.exists()) {
                for ((u, data) in demoUsers) db.collection("users").document(u).set(data, SetOptions.merge()).await()

                val child1 = mapOf("studentId" to 1, "fullName" to "John Johnson", "gradeName" to "Grade 10A", "parentUsername" to "sjohnson")
                val child2 = mapOf("studentId" to 2, "fullName" to "Emily Johnson", "gradeName" to "Grade 8B", "parentUsername" to "sjohnson")
                db.collection("students").document("1").set(child1).await()
                db.collection("students").document("2").set(child2).await()

                val m1 = mapOf("resultId" to 1, "studentId" to 1, "subject" to "Mathematics", "assessmentName" to "Term 1 Test", "assessmentType" to "Test", "markObtained" to 85.0, "totalMarks" to 100.0, "isApproved" to true, "timestamp" to System.currentTimeMillis())
                val m2 = mapOf("resultId" to 2, "studentId" to 1, "subject" to "Physical Sciences", "assessmentName" to "Practical Lab", "assessmentType" to "Practical", "markObtained" to 78.0, "totalMarks" to 100.0, "isApproved" to false, "timestamp" to System.currentTimeMillis() - 1000)
                db.collection("marks").document("mark_1").set(m1).await()
                db.collection("marks").document("mark_2").set(m2).await()

                val n1 = mapOf("noticeId" to 1, "noticeName" to "Term 4 Examination Timetable", "noticeType" to "Academic", "description" to "Final examination schedules for Grades 8–12 have been published.", "staffName" to "Jane Smith", "datePosted" to "2026-10-20", "isPinned" to true, "timestamp" to System.currentTimeMillis())
                val n2 = mapOf("noticeId" to 2, "noticeName" to "POPIA & Medical Consent Reminder", "noticeType" to "Urgent", "description" to "Parents are requested to verify annual medical and excursion consents.", "staffName" to "System Administrator", "datePosted" to "2026-10-18", "isPinned" to true, "timestamp" to System.currentTimeMillis() - 2000)
                db.collection("notices").document("notice_1").set(n1).await()
                db.collection("notices").document("notice_2").set(n2).await()

                val inv1 = mapOf("invoiceId" to 1, "studentId" to 1, "invoiceNumber" to "INV-2026-0001", "feeTypeName" to "Tuition", "description" to "Term 4 Tuition Fee", "amount" to 14500.0, "amountPaid" to 10000.0, "balance" to 4500.0, "dueDate" to "2026-11-01", "statusName" to "Partial")
                db.collection("invoices").document("inv_1").set(inv1).await()
            }
        } catch (_: Exception) {}
    }

    suspend fun login(username: String, pass: String): Result<UserSession> = withContext(Dispatchers.IO) {
        val clean = username.trim().lowercase()
        try {
            autoSeedSwaggerData()
            val snap = db.collection("users").document(clean).get().await()
            if (snap.exists() && snap.getString("password") == pass) {
                val role = snap.getString("userRole") ?: UserRoles.Student
                val fullName = snap.getString("fullName") ?: clean
                val userId = (snap.getLong("userId") ?: 1L).toInt()

                val children = mutableListOf<LinkedChildDto>()
                if (role == UserRoles.Parent || role == UserRoles.Student) {
                    val childSnaps = db.collection("students").whereEqualTo("parentUsername", if (role == UserRoles.Parent) clean else "sjohnson").get().await()
                    for (doc in childSnaps.documents) {
                        children.add(LinkedChildDto((doc.getLong("studentId") ?: 1L).toInt(), doc.getString("fullName") ?: "", doc.getString("gradeName") ?: "10A"))
                    }
                    if (children.isEmpty()) children.add(LinkedChildDto(1, "John Johnson", "Grade 10A"))
                }
                return@withContext Result.success(UserSession(userId = userId, username = clean, fullName = fullName, role = role, linkedChildren = children))
            }
        } catch (_: Exception) {}

        val demo = demoUsers[clean]
        if (demo != null && demo["password"] == pass) {
            val role = demo["userRole"] as String
            val children = if (role == UserRoles.Parent || role == UserRoles.Student) listOf(LinkedChildDto(1, "John Johnson", "Grade 10A"), LinkedChildDto(2, "Emily Johnson", "Grade 8B")) else emptyList()
            return@withContext Result.success(UserSession(userId = demo["userId"] as Int, username = clean, fullName = demo["fullName"] as String, role = role, linkedChildren = children))
        }
        Result.failure(Exception("Invalid username or password."))
    }

    suspend fun getMarks(studentId: Int, isParentOrStudent: Boolean): List<MarkRecordModel> = withContext(Dispatchers.IO) {
        try {
            val query = if (isParentOrStudent) {
                db.collection("marks").whereEqualTo("studentId", studentId).whereEqualTo("isApproved", true)
            } else {
                db.collection("marks").whereEqualTo("studentId", studentId)
            }
            val snap = query.get().await()
            val list = snap.documents.mapNotNull { d ->
                MarkRecordModel(
                    resultId = (d.getLong("resultId") ?: 1L).toInt(),
                    subject = d.getString("subject") ?: "Mathematics",
                    assessmentName = d.getString("assessmentName") ?: "Assessment",
                    assessmentType = d.getString("assessmentType") ?: "Test",
                    markObtained = d.getDouble("markObtained") ?: 80.0,
                    totalMarks = d.getDouble("totalMarks") ?: 100.0,
                    isApproved = d.getBoolean("isApproved") ?: true
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(MarkRecordModel(1, "Mathematics", "Term 1 Test", "Test", 85.0, 100.0, true))
    }

    suspend fun uploadMark(studentId: Int, mark: Double, isHod: Boolean): Result<String> = withContext(Dispatchers.IO) {
        try {
            val newDoc = db.collection("marks").document()
            newDoc.set(mapOf("resultId" to (10..999).random(), "studentId" to studentId, "subject" to "Information Technology", "assessmentName" to "Final Term Assessment", "assessmentType" to "Exam", "markObtained" to mark, "totalMarks" to 100.0, "isApproved" to isHod, "timestamp" to System.currentTimeMillis())).await()
            Result.success(if (isHod) "Mark approved & published." else "Mark submitted (Pending Head of Academics approval).")
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}"))
        }
    }

    suspend fun getNotices(): List<NoticeModel> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("notices").orderBy("timestamp", Query.Direction.DESCENDING).get().await()
            val list = snap.documents.mapNotNull { d ->
                NoticeModel(
                    noticeId = (d.getLong("noticeId") ?: 1L).toInt(),
                    noticeName = d.getString("noticeName") ?: "",
                    noticeType = d.getString("noticeType") ?: "General",
                    description = d.getString("description") ?: "",
                    staffName = d.getString("staffName") ?: "Administration",
                    datePosted = d.getString("datePosted") ?: "Today",
                    isPinned = d.getBoolean("isPinned") ?: false
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(NoticeModel(1, "Term 4 Examination Timetable", "Academic", "Final examination schedules for Grades 8–12 have been published.", "Jane Smith", "Today", true))
    }

    suspend fun postNotice(title: String, desc: String, author: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("notices").document().set(mapOf("noticeId" to (10..999).random(), "noticeName" to title, "noticeType" to "General", "description" to desc, "staffName" to author, "datePosted" to "Just now", "isPinned" to false, "timestamp" to System.currentTimeMillis())).await()
            Result.success("Notice published live to Firebase.")
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getInvoices(studentId: Int): List<InvoiceSummaryModel> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("invoices").whereEqualTo("studentId", studentId).get().await()
            val list = snap.documents.mapNotNull { d ->
                InvoiceSummaryModel(
                    invoiceId = (d.getLong("invoiceId") ?: 1L).toInt(),
                    invoiceNumber = d.getString("invoiceNumber") ?: "INV-2026",
                    feeTypeName = d.getString("feeTypeName") ?: "Tuition",
                    description = d.getString("description") ?: "",
                    amount = d.getDouble("amount") ?: 14500.0,
                    amountPaid = d.getDouble("amountPaid") ?: 10000.0,
                    balance = d.getDouble("balance") ?: 4500.0,
                    dueDate = d.getString("dueDate") ?: "2026-11-01",
                    statusName = d.getString("statusName") ?: "Partial"
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(InvoiceSummaryModel(1, "INV-2026-0001", "Tuition", "Term 4 Tuition Fee", 14500.0, 10000.0, 4500.0, "2026-11-01", "Partial"))
    }

    suspend fun uploadProofOfPayment(invoiceId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("proofs").document().set(mapOf(
                "invoiceId" to invoiceId,
                "status" to "Pending Verification",
                "timestamp" to System.currentTimeMillis()
            )).await()
            Result.success("Proof of Payment uploaded successfully.")
        } catch (_: Exception) {
            Result.failure(Exception("Upload failed."))
        }
    }

    suspend fun assignClassTeacher(grade: String, teacherName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            db.collection("class_teachers").document(grade).set(mapOf(
                "grade" to grade,
                "teacherName" to teacherName,
                "timestamp" to System.currentTimeMillis()
            )).await()
            Result.success("Class Teacher assigned successfully.")
        } catch (_: Exception) {
            Result.failure(Exception("Assignment failed."))
        }
    }

    suspend fun registerParent(user: String, pass: String, first: String, last: String, email: String, idNum: String, gender: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val clean = user.trim().lowercase()
            val exists = db.collection("users").document(clean).get().await().exists()
            if (exists) return@withContext Result.failure(Exception("Username already exists."))

            db.collection("users").document(clean).set(mapOf(
                "userId" to (100..999).random(),
                "username" to clean,
                "password" to pass,
                "fullName" to "$first $last",
                "email" to email,
                "userRole" to UserRoles.Parent,
                "idNumber" to idNum,
                "gender" to gender
            )).await()
            Result.success("Parent account registered successfully.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransportRoutes(): List<TransportRouteDto> = withContext(Dispatchers.IO) {
        listOf(
            TransportRouteDto("Route A - North Suburbs", "Mr. Smith", "082 123 4567", "15:30", "On Time"),
            TransportRouteDto("Route B - South Suburbs", "Mrs. Jones", "083 765 4321", "15:45", "Delayed (Traffic)"),
            TransportRouteDto("Route C - East Suburbs", "Mr. Brown", "084 555 1234", "15:30", "On Time")
        )
    }

    suspend fun getOpenIncidents(): List<IncidentDto> = withContext(Dispatchers.IO) {
        try {
            val snap = db.collection("incidents").whereEqualTo("status", "Open").get().await()
            val list = snap.documents.mapNotNull { d ->
                IncidentDto(
                    incidentId = (d.getLong("incidentId") ?: 1L).toInt(),
                    studentName = d.getString("studentName") ?: "Unknown",
                    category = d.getString("category") ?: "General",
                    description = d.getString("description") ?: "",
                    severity = (d.getLong("severity") ?: 1L).toInt(),
                    status = d.getString("status") ?: "Open",
                    occurredAt = d.getString("occurredAt") ?: "Today"
                )
            }
            if (list.isNotEmpty()) return@withContext list
        } catch (_: Exception) {}

        listOf(
            IncidentDto(1, "John Johnson", "Behavior", "Late for morning registration", 1, "Open", "Today")
        )
    }

    suspend fun getMedia(): List<com.example.nhsentranet.model.MediaDto> = withContext(Dispatchers.IO) {
        listOf(
            com.example.nhsentranet.model.MediaDto(1, "Inter-House Athletics", "Photo", "azure_blob_url_1"),
            com.example.nhsentranet.model.MediaDto(2, "Grease Production Trailer", "Video", "azure_blob_url_2"),
            com.example.nhsentranet.model.MediaDto(3, "Student Wellness Ep 1", "Podcast", "azure_blob_url_3")
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: NewtonFirebaseRepository? = null

        fun getInstance(): NewtonFirebaseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = NewtonFirebaseRepository()
                INSTANCE = instance
                instance
            }
        }
    }
}
