package xyz.ezsky.ybutools.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.ezsky.ybutools.data.jsxt.entity.Grades
import xyz.ezsky.ybutools.data.jsxt.entity.GradeDao

/**
 * 数据库
 */
@Database(entities = [Grades::class], version = 1)
abstract class GradeDatabase : RoomDatabase() {
    abstract fun gradeDao(): GradeDao
}

object DatabaseProvider {
    private var database: GradeDatabase? = null

    fun getDatabase(context: Context): GradeDatabase {
        if (database == null) {
            database = Room.databaseBuilder(
                context.applicationContext,
                GradeDatabase::class.java,
                "grades.db"
            ).build()
        }
        return database!!
    }
}