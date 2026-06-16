package com.dionisispx.expensetracker.di

import android.app.Application
import androidx.room.Room
import com.dionisispx.expensetracker.data.local.ExpenseDao
import com.dionisispx.expensetracker.data.local.ExpenseDatabase
import com.dionisispx.expensetracker.data.repository.ExpenseRepositoryImpl
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindVisionRepository(
        visionRepositoryImpl: com.dionisispx.expensetracker.data.repository.VisionRepositoryImpl
    ): com.dionisispx.expensetracker.domain.repository.VisionRepository

    @Binds
    @Singleton
    abstract fun bindImageProcessor(
        androidImageProcessor: com.dionisispx.expensetracker.data.util.AndroidImageProcessor
    ): com.dionisispx.expensetracker.domain.util.ImageProcessor

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: com.dionisispx.expensetracker.data.repository.UserPreferencesRepositoryImpl
    ): com.dionisispx.expensetracker.domain.repository.UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideExpenseDatabase(app: Application): ExpenseDatabase {
            return Room.databaseBuilder(
                app,
                ExpenseDatabase::class.java,
                "expense_db"
            )
            .fallbackToDestructiveMigration() // Added this to fix the schema crash
            .build()
        }

        @Provides
        @Singleton
        fun provideExpenseDao(db: ExpenseDatabase): ExpenseDao {
            return db.dao
        }
    }
}
