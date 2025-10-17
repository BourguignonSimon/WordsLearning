package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        RecordingSessionEntity::class,
        RecordingSegmentEntity::class,
        RecordingSessionFtsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingSessionDao(): RecordingSessionDao

    companion object {
        private const val DATABASE_NAME = "offline_hqasr.db"

        fun build(context: Context, passphrase: CharArray): AppDatabase {
            SQLiteDatabase.loadLibs(context)
            val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase))
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
