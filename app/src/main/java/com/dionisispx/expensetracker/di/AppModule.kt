package com.dionisispx.expensetracker.di

import android.app.Application
import androidx.room.Room
import com.dionisispx.expensetracker.data.local.ExpenseDao
import com.dionisispx.expensetracker.data.local.ExpenseDatabase
import com.dionisispx.expensetracker.data.repository.ExpenseRepositoryImpl
import com.dionisispx.expensetracker.domain.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Module to tell hilt how to provide dependencies
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide the room database instance as a singleton
    @Provides
    @Singleton
    fun provideExpenseDatabase(app: Application): ExpenseDatabase {
        return Room.databaseBuilder(
            app,
            ExpenseDatabase::class.java,
            "expense_db"
        ).build()
    }

    // Provide the DAO to be used in Viewmodels
    @Provides
    @Singleton
    fun provideExpenseDao(db: ExpenseDatabase): ExpenseDao {
        return db.dao
    }

    // Provide the repository to the rest of the app
    @Provides
    @Singleton
    fun provideExpenseRepository(dao: ExpenseDao): ExpenseRepository {
        return ExpenseRepositoryImpl(dao)
    }
}