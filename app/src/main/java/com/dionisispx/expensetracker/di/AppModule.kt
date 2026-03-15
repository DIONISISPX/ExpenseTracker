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
