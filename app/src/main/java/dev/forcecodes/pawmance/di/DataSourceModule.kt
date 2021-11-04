package dev.forcecodes.pawmance.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.pawmance.data.GaleShapelyMatchDataSource
import dev.forcecodes.pawmance.data.PetMatchDataSource
import dev.forcecodes.pawmance.data.auth.AuthStateDataSource
import dev.forcecodes.pawmance.data.auth.FirebaseAuthStateDataSource
import dev.forcecodes.pawmance.data.auth.FirestorePetInfoStateDataSource
import dev.forcecodes.pawmance.data.auth.PetInfoStateDataSource
import dev.forcecodes.pawmance.data.conversation.ConversationRepository
import dev.forcecodes.pawmance.data.conversation.ConversationRepositoryImpl
import dev.forcecodes.pawmance.data.info.PetInfoRepository
import dev.forcecodes.pawmance.data.info.PetInfoRepositoryImpl
import dev.forcecodes.pawmance.data.location.LocationSearchRepository
import dev.forcecodes.pawmance.data.location.LocationSearchRepositoryImpl
import dev.forcecodes.pawmance.data.progress.PetProgressDataSource
import dev.forcecodes.pawmance.data.progress.PetProgressDataSourceImpl

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
}