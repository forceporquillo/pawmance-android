package dev.apes.pawmance.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.apes.pawmance.data.GaleShapelyMatchDataSource
import dev.apes.pawmance.data.PetMatchDataSource
import dev.apes.pawmance.data.auth.AuthStateDataSource
import dev.apes.pawmance.data.auth.FirebaseAuthStateDataSource
import dev.apes.pawmance.data.auth.FirestorePetInfoStateDataSource
import dev.apes.pawmance.data.auth.PetInfoStateDataSource
import dev.apes.pawmance.data.conversation.ConversationRepository
import dev.apes.pawmance.data.conversation.ConversationRepositoryImpl
import dev.apes.pawmance.data.conversation.StreamClientConnector
import dev.apes.pawmance.data.conversation.StreamClientConnectorImpl
import dev.apes.pawmance.data.info.PetInfoRepository
import dev.apes.pawmance.data.info.PetInfoRepositoryImpl
import dev.apes.pawmance.data.location.LocationSearchRepository
import dev.apes.pawmance.data.location.LocationSearchRepositoryImpl
import dev.apes.pawmance.data.progress.PetProgressDataSource
import dev.apes.pawmance.data.progress.PetProgressDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

  @Binds
  abstract fun providesAuthDataSource(
    authStateDataSource: FirebaseAuthStateDataSource
  ): AuthStateDataSource

  @Binds
  abstract fun providesLocationSearchRepository(
    locationSearchRepositoryImpl: LocationSearchRepositoryImpl
  ): LocationSearchRepository

  @Binds
  abstract fun providesPetInfoRepository(
    petInfoRepositoryImpl: PetInfoRepositoryImpl
  ): PetInfoRepository

  @Binds
  abstract fun providesPetInfoStateDataSource(
    petInfoStateDataSource: FirestorePetInfoStateDataSource
  ): PetInfoStateDataSource

  @Binds
  abstract fun providesGaleShapelyMatchDataSource(
    petMatchDataSource: PetMatchDataSource
  ): GaleShapelyMatchDataSource

  @Binds
  abstract fun providesConversationRepository(
    conversationRepository: ConversationRepositoryImpl
  ): ConversationRepository

  @Binds
  abstract fun providesPetProgressDataSource(
    petProgressDataSourceImpl: PetProgressDataSourceImpl
  ): PetProgressDataSource

  @Binds
  abstract fun providesStreamClientConnector(
    streamClientConnectorImpl: StreamClientConnectorImpl
  ): StreamClientConnector
}