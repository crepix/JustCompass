package crepix.java_conf.gr.jp.justcompass.di

import android.content.Context
import android.hardware.SensorManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvideModule {

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context) = context.getSystemService(SensorManager::class.java)!!
}
