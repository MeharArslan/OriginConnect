package com.meharenterprises.originconnect.di
import android.content.Context
import androidx.room.Room
import com.meharenterprises.originconnect.BuildConfig
import com.meharenterprises.originconnect.data.local.OcContactDao
import com.meharenterprises.originconnect.data.local.OcConversationDao
import com.meharenterprises.originconnect.data.local.OcDatabase
import com.meharenterprises.originconnect.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOcDatabase(@ApplicationContext ctx: Context): OcDatabase =
        Room.databaseBuilder(ctx, OcDatabase::class.java, "oc_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideConversationDao(db: OcDatabase): OcConversationDao = db.conversationDao()

    @Provides @Singleton
    fun provideContactDao(db: OcDatabase): OcContactDao = db.contactDao()

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}
