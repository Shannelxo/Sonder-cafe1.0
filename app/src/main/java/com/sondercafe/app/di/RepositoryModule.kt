package com.sondercafe.app.di

import com.sondercafe.app.core.notifications.AndroidOrderNotifier
import com.sondercafe.app.data.repository.AuthRepositoryImpl
import com.sondercafe.app.data.repository.CartRepositoryImpl
import com.sondercafe.app.data.repository.FeedbackRepositoryImpl
import com.sondercafe.app.data.repository.MenuRepositoryImpl
import com.sondercafe.app.data.repository.OrderRepositoryImpl
import com.sondercafe.app.data.repository.WeatherRepositoryImpl
import com.sondercafe.app.data.work.WorkManagerOrderSimulator
import com.sondercafe.app.domain.repository.AuthRepository
import com.sondercafe.app.domain.repository.CartRepository
import com.sondercafe.app.domain.repository.FeedbackRepository
import com.sondercafe.app.domain.repository.MenuRepository
import com.sondercafe.app.domain.repository.OrderRepository
import com.sondercafe.app.domain.repository.WeatherRepository
import com.sondercafe.app.domain.service.OrderNotifier
import com.sondercafe.app.domain.service.OrderSimulator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Tells Hilt which implementation to use for each domain interface. This is the only place
 * where the domain layer's contracts are tied to the Android and Room implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindMenuRepository(impl: MenuRepositoryImpl): MenuRepository

    @Binds
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds
    abstract fun bindFeedbackRepository(impl: FeedbackRepositoryImpl): FeedbackRepository

    @Binds
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    abstract fun bindOrderNotifier(impl: AndroidOrderNotifier): OrderNotifier

    @Binds
    abstract fun bindOrderSimulator(impl: WorkManagerOrderSimulator): OrderSimulator
}
