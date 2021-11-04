package dev.forcecodes.pawmance.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.pawmance.data.auth.AuthStateDataSource
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegate
import dev.forcecodes.pawmance.ui.signin.SignInViewModelDelegateImpl
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