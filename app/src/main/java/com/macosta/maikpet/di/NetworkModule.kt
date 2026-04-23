package com.macosta.maikpet.di

import android.content.Context
import com.macosta.maikpet.data.api.MaikPetApi
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://lmcosturas.com/pet/"
    private const val SESSION_PREFS = "session_prefs"
    private const val SESSION_ID_KEY = "session_id"
    private const val AUTH_TOKEN_KEY = "auth_token"
    private const val CACHED_USER_ID = "cached_user_id"
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val sessionInterceptor = Interceptor { chain ->
            val sessionPrefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
            val sessionId = sessionPrefs.getString(SESSION_ID_KEY, null)
            val authToken = sessionPrefs.getString(AUTH_TOKEN_KEY, null)
            val userId = sessionPrefs.getInt(CACHED_USER_ID, -1).takeIf { it > 0 }
            
            val request = chain.request().newBuilder().apply {
                if (!sessionId.isNullOrEmpty()) {
                    addHeader("X-Session-Id", sessionId)
                }
                if (!authToken.isNullOrEmpty()) {
                    addHeader("X-Auth-Token", authToken)
                }
                if (userId != null) {
                    addHeader("X-User-Id", userId.toString())
                }
            }.build()
            chain.proceed(request)
        }
        
        val cookieJar = object : CookieJar {
            private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()
            
            override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies.toMutableList()
            }
            
            override fun loadForRequest(url: okhttp3.HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        }
        
        return OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(sessionInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMaikPetApi(retrofit: Retrofit): MaikPetApi {
        return retrofit.create(MaikPetApi::class.java)
    }
}