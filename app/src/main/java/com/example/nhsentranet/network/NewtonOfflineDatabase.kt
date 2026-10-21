package com.example.nhsentranet.network

import kotlinx.coroutines.delay

object UserRoles {
    const val Admin = "Admin"
    const val Teacher = "Teacher"
    const val HeadOfDepartment = "Head of Academics"
    const val Parent = "Parent"
    const val Student = "Student"
}

data class DashboardStatsModel(
    val fullName: String = "",
    val userRole: String = "",
    val totalUsers: Int = 0,
    val students: Int = 0,
    val parents: Int = 0,
    val pendingApprovals: Int = 0,
    val teachersCount: Int = 0,
    val attendanceRate: Double = 96.4,
    val classesToday: Int = 0,
    val pendingMarking: Int = 0,
    val upcomingAssessments: List<String> = emptyList()
)

data class MarkRecordModel(
    val resultId: Int,
    val subject: String,
    val assessmentName: String,
    val assessmentType: String,
    val markObtained: Double,
    val totalMarks: Double,
    val isApproved: Boolean = true
)

data class SubjectSummaryModel(
    val subjectId: Int,
    val subjectName: String,
    val subjectType: String,
    val teacherName: String,
    val currentMark: Double
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

data class TeacherContactModel(
    val staffId: Int,
    val fullname: String,
    val jobRole: String,
    val email: String,
    val contactNumber: String,
    val subjectsTaught: List<String>
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

object NewtonOfflineDatabase {
    val marksTable = mutableListOf(
        MarkRecordModel(1, "Mathematics", "Term 1 Test", "Test", 85.0, 100.0),
        MarkRecordModel(2, "Physical Sciences", "Practical Investigation", "Practical", 78.0, 100.0)
    )

    val noticesTable = mutableListOf(
        NoticeModel(1, "Term 4 Examination Timetable", "Academic", "Final examination schedules have been published.", "Jane Smith", "Today", true),
        NoticeModel(2, "POPIA & Medical Consent Reminder", "Urgent", "Please verify annual medical consents.", "System Administrator", "Yesterday", true)
    )

    suspend fun login(username: String, password: String): Result<UserSession> {
        delay(600)
        val cleanUser = username.trim().lowercase()
        val accounts = mapOf(
            "admin" to Triple("Admin@123", UserRoles.Admin, "System Administrator"),
            "jsmith" to Triple("Teacher@123", UserRoles.Teacher, "Jane Smith"),
            "john.johnson" to Triple("Student@123", UserRoles.Student, "John Johnson"),
            "sjohnson" to Triple("Parent@123", UserRoles.Parent, "Sarah Johnson"),
            "hod" to Triple("Head@123", UserRoles.HeadOfDepartment, "Dr. Adams"),
            "academics" to Triple("Head@123", UserRoles.HeadOfDepartment, "Dr. Adams")
        )

        val match = accounts[cleanUser]
        if (match != null && match.first == password) {
            val children = if (match.second == UserRoles.Parent || match.second == UserRoles.Student) {
                listOf(
                    LinkedChildDto(1, "John Johnson", "Grade 10A"),
                    LinkedChildDto(2, "Emily Johnson", "Grade 8B")
                )
            } else emptyList()
            return Result.success(UserSession(1, cleanUser, match.third, match.second, token = "offline-token", linkedChildren = children))
        }
        return Result.failure(Exception("Invalid username or password."))
    }

    suspend fun getDashboardStats(session: UserSession): DashboardStatsModel {
        return DashboardStatsModel(
            fullName = session.fullName,
            userRole = session.userRole,
            totalUsers = 48, students = 30, parents = 25, pendingApprovals = 3,
            teachersCount = 10, attendanceRate = 96.4, classesToday = 5, pendingMarking = 3,
            upcomingAssessments = listOf("Mathematics: Term Test 1", "Physical Sciences: Lab")
        )
    }

    suspend fun uploadStudentMark(mark: Double): Result<String> {
        delay(400)
        marksTable.add(MarkRecordModel(marksTable.size + 1, "Information Technology", "Recent Assessment", "Test", mark, 100.0))
        return Result.success("Mark ($mark%) saved locally to device database.")
    }

    suspend fun createNotice(title: String, desc: String, author: String) {
        delay(300)
        noticesTable.add(0, NoticeModel(noticesTable.size + 1, title, "General", desc, author, "Just now", false))
    }
}
