package com.arjun.janio.di

import android.content.Context
import com.arjun.janio.db.JanioDao
import com.arjun.janio.db.JanioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): JanioDao =
        JanioDatabase.getInstance(context).janioDao

}