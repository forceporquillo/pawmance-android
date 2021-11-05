package dev.apes.pawmance.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.apes.pawmance.data.conversation.ConversationsDao
import dev.apes.pawmance.data.progress.PetProgress
import dev.apes.pawmance.data.progress.ProgressDao
import dev.apes.pawmance.model.Conversations

@Database(
  entities = [
    Conversations::class,
    PetProgress::class
  ],
  version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun conversationDao(): ConversationsDao
  abstract fun progressDao(): ProgressDao

  companion object {
    private var INSTANCE: AppDatabase? = null

    @JvmStatic
    fun getInstance(context: Context): AppDatabase {
      synchronized(AppDatabase::class) {
        if (INSTANCE == null) {
          val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "conversation.db"
          )
            .fallbackToDestructiveMigration()
            .build()

          INSTANCE = database
        }
      }
      return INSTANCE!!
    }
  }
}
