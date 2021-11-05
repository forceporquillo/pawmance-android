package dev.apes.pawmance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.apes.pawmance.data.auth.AuthStateDataSource
import dev.apes.pawmance.ui.signin.SignInViewModelDelegate
import dev.apes.pawmance.ui.signin.SignInViewModelDelegateImpl
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SignInViewModelDelegateModule {

  @Singleton
  @Provides
  fun providesSignInViewModelDelegate(
    authStateDataSource: AuthStateDataSource,
    @ApplicationScope applicationScope: CoroutineScope
  ): SignInViewModelDelegate {
    return SignInViewModelDelegateImpl(
      authStateDataSource, applicationScope
    )
  }
}