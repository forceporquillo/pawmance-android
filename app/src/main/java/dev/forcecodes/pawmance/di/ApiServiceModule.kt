package dev.forcecodes.pawmance.di

import android.os.Looper
import com.devforcecodes.pawmance.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.pawmance.api.PlacesApiService
import dev.forcecodes.pawmance.api.PushMessageNotifierApi
import okhttp3.Call
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiServiceModule {

  @InternalApi
  @Singleton
  @Provides
  internal fun providesGson(): Gson {
    return GsonBuilder().apply {
      serializeNulls()
      setLenient()
    }.create()
  }

  @PlacesRetrofit
  @Provides
  internal fun providesRetrofitForPlaces(
    @InternalApi gson: Gson,
    @InternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit = createRetrofitInstance(gson, okHttpClient, BuildConfig.BASE_URL_PLACES)

  @FcmRetrofit
  @Provides
  internal fun providesRetrofitForFcm(
    @InternalApi gson: Gson,
    @InternalApi okHttpClient: Lazy<OkHttpClient>
  ): Retrofit = createRetrofitInstance(gson, okHttpClient, BuildConfig.BASE_URL_FCM)

  private fun createRetrofitInstance(
    gson: Gson,
    okHttpClient: Lazy<OkHttpClient>,
    baseUrl: String
  ): Retrofit {
    return Retrofit.Builder().apply {
      baseUrl(baseUrl)
      addConverterFactory(GsonConverterFactory.create(gson))
      delegatingCallFactory(okHttpClient)
    }.build()
  }

  @InternalApi
  @Provides
  internal fun providesOkHttpLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
  }

  @InternalApi
  @Singleton
  @Provides
  internal fun providesOkHttpClient(
    @InternalApi interceptor: HttpLoggingInterceptor
  ): OkHttpClient {
    return checkMainThread {
      OkHttpClient.Builder().apply {
        connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
        writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
          addInterceptor(interceptor)
        }

        addInterceptor(Interceptor { chain ->
          val request = chain.request().newBuilder()
            .addHeader("Accept-Encoding", "identity")
            .build()

          chain.proceed(request)
        })
      }.build()
    }
  }

  @PlacesBackendApi
  @Singleton
  @Provides
  internal fun providesPlacesApiService(
    @PlacesRetrofit retrofit: Retrofit
  ): PlacesApiService = retrofit.create()

  @FcmMessageService
  @Singleton
  @Provides
  internal fun providesMessageService(
    @FcmRetrofit retrofit: Retrofit
  ): PushMessageNotifierApi = retrofit.create()
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class PlacesBackendApi

@Retention(AnnotationRetention.BINARY)
@Qualifier
internal annotation class FcmMessageService

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class InternalApi

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class PlacesRetrofit

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class FcmRetrofit

internal const val DEFAULT_TIMEOUT = 15L

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun <T> checkMainThread(block: () -> T): T =
  if (Looper.myLooper() == Looper.getMainLooper()) {
    throw IllegalStateException("Object initialized on main thread.")
  } else {
    block()
  }

@PublishedApi
internal inline fun Retrofit.Builder.callFactory(
  crossinline body: (Request) -> Call
) = callFactory { request -> body(request) }

@Suppress("NOTHING_TO_INLINE")
inline fun Retrofit.Builder.delegatingCallFactory(
  delegate: Lazy<OkHttpClient>
): Retrofit.Builder = callFactory {
  delegate.get().newCall(it)
}
