package dev.forcecodes.pawmance.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.pawmance.data.PetMatchDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class AlgorithmModule {

  @Binds
  abstract fun providesGaleShapelyMatchDataSource(
    galeShapelyAlgorithmImpl: PetMatchDataSource.GaleShapelyAlgorithmImpl
  ): PetMatchDataSource.GaleShapelyAlgorithm
}