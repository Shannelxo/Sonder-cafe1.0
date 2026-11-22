package com.sondercafe.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sondercafe.app.data.local.dao.CartDao
import com.sondercafe.app.data.local.dao.FeedbackDao
import com.sondercafe.app.data.local.dao.MenuDao
import com.sondercafe.app.data.local.dao.OrderDao
import com.sondercafe.app.data.local.dao.UserDao
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.local.entity.FeedbackEntity
import com.sondercafe.app.data.local.entity.MenuItemEntity
import com.sondercafe.app.data.local.entity.OrderEntity
import com.sondercafe.app.data.local.entity.OrderItemEntity
import com.sondercafe.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MenuItemEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        FeedbackEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SonderCafeDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun menuDao(): MenuDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun feedbackDao(): FeedbackDao

    companion object {
        const val NAME = "sonder_cafe.db"
    }
}
