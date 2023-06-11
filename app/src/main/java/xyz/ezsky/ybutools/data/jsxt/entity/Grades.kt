package xyz.ezsky.ybutools.data.jsxt.entity

import androidx.room.*

@Entity(tableName = "grades")
data class Grades(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listid: String,
    val courseName: String,
    val score: String
)
data class Gradesinfo(
    val credit: Double,
    val averageScore: Double,
    val gpa: Double
)
@Dao
interface GradeDao {
    @Insert
    fun insertGrades(grade: Grades)

    @Query("SELECT * FROM grades")
    fun getAllGrades(): List<Grades>
    @Query("DELETE FROM grades")
    suspend fun deleteAllGrades()
    @Query("SELECT COUNT(*) FROM grades")
    suspend fun getGradesCount(): Int
}


