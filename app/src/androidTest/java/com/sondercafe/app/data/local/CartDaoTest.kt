package com.sondercafe.app.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sondercafe.app.data.local.dao.CartDao
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.local.entity.UserEntity
import com.sondercafe.app.data.local.seed.MenuSeedData
import com.sondercafe.app.domain.model.Role
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CartDaoTest {

    private lateinit var db: SonderCafeDatabase
    private lateinit var cartDao: CartDao
    private var userId = 0L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SonderCafeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cartDao = db.cartDao()
        db.menuDao().insertAll(MenuSeedData.items)
        userId = db.userDao().insert(
            UserEntity(email = "sam@example.com", displayName = "Sam", passwordHash = "h", passwordSalt = "s", role = Role.CUSTOMER, createdAt = Instant.now())
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun cartItem(menuItemId: Long, quantity: Int, addedAt: Long = 0) =
        CartItemEntity(userId, menuItemId, quantity, Instant.ofEpochMilli(addedAt))

    @Test
    fun cartLoadsWithItsMenuItems() = runTest {
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 2))

        val cart = cartDao.observeCart(userId).first()

        assertEquals(1, cart.size)
        assertEquals("Latte", cart.single().menuItem.name)
        assertEquals(2, cart.single().cartItem.quantity)
    }

    @Test
    fun upsertOnTheSameItemReplacesInsteadOfDuplicating() = runTest {
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 1))
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 3))

        val cart = cartDao.getCart(userId)

        assertEquals(1, cart.size)
        assertEquals(3, cart.single().cartItem.quantity)
    }

    @Test
    fun cartIsOrderedByWhenItemsWereAdded() = runTest {
        cartDao.upsert(cartItem(menuItemId = 16, quantity = 1, addedAt = 2_000))
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 1, addedAt = 1_000))

        val names = cartDao.getCart(userId).map { it.menuItem.name }

        assertEquals(listOf("Latte", "Chocolate brownie"), names)
    }

    @Test
    fun updateAndDeleteAffectOnlyTheTargetRow() = runTest {
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 1))
        cartDao.upsert(cartItem(menuItemId = 16, quantity = 1))

        assertEquals(1, cartDao.updateQuantity(userId, 4, 5))
        assertEquals(1, cartDao.delete(userId, 16))

        assertEquals(5, cartDao.find(userId, 4)?.quantity)
        assertNull(cartDao.find(userId, 16))
    }

    @Test
    fun deletingTheUserEmptiesTheirCart() = runTest {
        cartDao.upsert(cartItem(menuItemId = 4, quantity = 1))

        db.userDao().deleteById(userId)

        assertTrue(cartDao.getCart(userId).isEmpty())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun cartItemForAMissingMenuItemIsRejected() = runTest {
        cartDao.upsert(cartItem(menuItemId = 999, quantity = 1))
    }
}
