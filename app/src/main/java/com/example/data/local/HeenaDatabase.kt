package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ChatMessageEntity::class,
        ConversationEntity::class,
        UserMemoryEntity::class,
        RoutineEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HeenaDatabase : RoomDatabase() {

    abstract fun chatDao(): ChatDao
    abstract fun memoryDao(): MemoryDao
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var instance: HeenaDatabase? = null

        fun getInstance(context: Context): HeenaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    HeenaDatabase::class.java,
                    "heena_ai_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default sample routines
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).routineDao()
                                dao.insertRoutine(
                                    RoutineEntity(
                                        title = "Good Morning / सुप्रभात",
                                        voiceTrigger = "Good Morning",
                                        description = "Daily greeting, date announcement, and motivating quote in Hindi.",
                                        isEnabled = true,
                                        actionsJson = """[{"action":"GREETING","param":"नमस्ते! शुभ प्रभात! आपका दिन मंगलमय हो।"},{"action":"DATE_TIME"},{"action":"SUGGESTION","param":"आज का शेड्यूल देखें"}]"""
                                    )
                                )
                                dao.insertRoutine(
                                    RoutineEntity(
                                        title = "YouTube Music / संगीत",
                                        voiceTrigger = "Music Sunao",
                                        description = "Launches YouTube with popular Bollywood chill songs.",
                                        isEnabled = true,
                                        actionsJson = """[{"action":"OPEN_APP","param":"com.google.android.youtube"},{"action":"SEARCH_YOUTUBE","param":"Bollywood chill lofi songs"}]"""
                                    )
                                )
                                dao.insertRoutine(
                                    RoutineEntity(
                                        title = "Focus 25m Timer",
                                        voiceTrigger = "Focus Time",
                                        description = "Starts a 25-minute Pomodoro study focus session.",
                                        isEnabled = true,
                                        actionsJson = """[{"action":"SET_TIMER","param":"1500"}]"""
                                    )
                                )
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
