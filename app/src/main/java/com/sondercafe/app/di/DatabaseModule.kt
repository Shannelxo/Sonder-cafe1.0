package com.sondercafe.app.di

import android.content.Context
import androidx.room.Room
import com.sondercafe.app.data.local.SonderCafeDatabase
import com.sondercafe.app.data.local.dao.CartDao
import com.sondercafe.app.data.local.dao.FeedbackDao
import com.sondercafe.app.data.local.dao.MenuDao
import com.sondercafe.app.data.local.dao.OrderDao
import com.sondercafe.app.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // No fallbackToDestructiveMigration(): a schema change without a migration should fail
    // loudly during development, not quietly wipe customers' accounts and orders.
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonderCafeDatabase =
        Room.databaseBuilder(context, SonderCafeDatabase::class.java, SonderCafeDatabase.NAME).build()

    @Provides
    fun provideUserDao(db: SonderCafeDatabase): UserDao = db.userDao()

    @Provides
    fun provideMenuDao(db: SonderCafeDatabase): MenuDao = db.menuDao()

    @Provides
    fun provideCartDao(db: SonderCafeDatabase): CartDao = db.cartDao()

    @Provides
    fun provideOrderDao(db: SonderCafeDatabase): OrderDao = db.orderDao()

    @Provides
    fun provideFeedbackDao(db: SonderCafeDatabase): FeedbackDao = db.feedbackDao()
}
