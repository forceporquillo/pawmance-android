package dev.apes.pawmance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.ChatDomain
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StreamModule {

  @Provides
  @Singleton
  fun provideStreamChatClient(): ChatClient {
    return ChatClient.instance()
  }

  @Provides
  @Singleton
  fun provideStreamChatDomain(): ChatDomain {
    return ChatDomain.instance()
  }
}