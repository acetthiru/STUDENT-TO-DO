package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.ExamDao
import com.example.data.dao.FocusDao
import com.example.data.dao.TaskDao
import com.example.data.dao.TaskLogDao
import com.example.data.model.ExamSubject
import com.example.data.model.Flashcard
import com.example.data.model.FocusSession
import com.example.data.model.ScannedMaterial
import com.example.data.model.Task
import com.example.data.model.TaskLog

@Database(
    entities = [
        Task::class,
        FocusSession::class,
        TaskLog::class,
        ExamSubject::class,
        ScannedMaterial::class,
        Flashcard::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun focusDao(): FocusDao
    abstract fun taskLogDao(): TaskLogDao
    abstract fun examDao(): ExamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "do_it_on_time_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
