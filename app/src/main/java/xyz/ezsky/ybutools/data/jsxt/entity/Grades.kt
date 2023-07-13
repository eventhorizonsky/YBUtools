package xyz.ezsky.ybutools.data.jsxt.entity

import androidx.room.*

@Entity(tableName = "grades")
data class Grades(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listid: String,
    val kkxq:String,//课程学期
    val kcbh:String,//课程编号
    val courseName: String,//课程名称
    val score: String,//成绩
    val tsyy:String,//特殊原因
    val xf:String,//学分
    val zxs:String,//总学时
    val jd:String,//绩点
    val bcxq:String,//补重学期
    val khfs:String,//考核方式
    val ksxz:String,//考试性质
    val kcsx:String,//课程属性
    val kcxz:String//课程性质
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


