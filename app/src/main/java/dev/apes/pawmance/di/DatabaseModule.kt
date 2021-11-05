package dev.apes.pawmance.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.apes.pawmance.data.conversation.ConversationsDao
import dev.apes.pawmance.data.progress.ProgressDao
import dev.apes.pawmance.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

  @Singleton
  @Provides
  fun providesConversationDao(
    @ApplicationContext context: Context
  ): ConversationsDao {
    return AppDatabase.getInstance(context).conversationDao()
  }

  @Singleton
  @Provides
  fun providesProgressDao(
    @ApplicationContext context: Context
  ): ProgressDao {
    return AppDatabase.getInstance(context).progressDao()
  }
}