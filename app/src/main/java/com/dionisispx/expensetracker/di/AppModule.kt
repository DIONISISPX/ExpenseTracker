package com.dionisispx.expensetracker.di

import android.app.Application
import androidx.room.Room
import com.dionisispx.expensetracker.data.local.ExpenseDao
import com.dionisispx.expensetracker.data.local.ExpenseDatabase
import com.dionisispx.expensetracker.data.repository.ExpenseRepositoryImpl
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import com.dionisispx.expensetracker.data.repository.VisionRepositoryImpl
import com.dionisispx.expensetracker.domain.repository.VisionRepository
import com.dionisispx.expensetracker.data.util.AndroidImageProcessor
import com.dionisispx.expensetracker.domain.util.ImageProcessor
import com.dionisispx.expensetracker.data.repository.UserPreferencesRepositoryImpl
import com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository
import com.dionisispx.expensetracker.data.remote.VisionApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule { // Dagger Hilt module for dependency injection

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindVisionRepository(
        visionRepositoryImpl: VisionRepositoryImpl
    ): VisionRepository

    @Binds
    @Singleton
    abstract fun bindImageProcessor(
        androidImageProcessor: AndroidImageProcessor
    ): ImageProcessor

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideExpenseDatabase(app: Application): ExpenseDatabase {
            return Room.databaseBuilder(
                app,
                ExpenseDatabase::class.java,
                "expense_db"
            )
            .fallbackToDestructiveMigration() // Fix schema crash
            .build()
        }

        @Provides
        @Singleton
        fun provideExpenseDao(db: ExpenseDatabase): ExpenseDao {
            return db.dao
        }

        @Provides
        @Singleton
        fun provideVisionApi(): VisionApi {
            return retrofit2.Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(VisionApi::class.java)
        }
    }
}
