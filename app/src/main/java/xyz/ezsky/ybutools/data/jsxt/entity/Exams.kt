package xyz.ezsky.ybutools.data.jsxt.entity

data class Exams(
    val id: String,
    val campus: String,
    val examSession: String,
    val courseCode: String,
    val courseName: String,
    val teacher: String,
    val examMode: String,
    val examTime: String,
    val examRoom: String,
    val seatNumber: String,
    val admissionTicket: String,
    val note: String
)
