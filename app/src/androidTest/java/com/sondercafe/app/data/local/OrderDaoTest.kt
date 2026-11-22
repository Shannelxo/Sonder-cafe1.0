package com.sondercafe.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sondercafe.app.data.local.dao.OrderDao
import com.sondercafe.app.data.local.entity.CartItemEntity
import com.sondercafe.app.data.local.entity.OrderEntity
import com.sondercafe.app.data.local.entity.OrderItemEntity
import com.sondercafe.app.data.local.entity.UserEntity
import com.sondercafe.app.data.local.seed.MenuSeedData
import com.sondercafe.app.domain.model.OrderStatus
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
class OrderDaoTest {

    private lateinit var db: SonderCafeDatabase
    private lateinit var orderDao: OrderDao
    private var alice = 0L
    private var bob = 0L

    @Before
    fun setUp() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SonderCafeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        orderDao = db.orderDao()
        db.menuDao().insertAll(MenuSeedData.items)
        alice = db.userDao().insert(user("alice@example.com"))
        bob = db.userDao().insert(user("bob@example.com"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun user(email: String) =
        UserEntity(email = email, displayName = "Test", passwordHash = "h", passwordSalt = "s", role = Role.CUSTOMER, createdAt = Instant.now())

    private fun order(userId: Long, createdAt: Long = 0, total: Int = 0) = OrderEntity(
        userId = userId,
        status = OrderStatus.PENDING,
        totalPence = total,
        createdAt = Instant.ofEpochMilli(createdAt),
        updatedAt = Instant.ofEpochMilli(createdAt)
    )

    private fun line(menuItemId: Long, quantity: Int, price: Int = 300) =
        OrderItemEntity(orderId = 0, menuItemId = menuItemId, nameSnapshot = "Item $menuItemId", unitPricePence = price, quantity = quantity)

    @Test
    fun placeOrderSavesLinesAndEmptiesTheCart() = runTest {
        db.cartDao().upsert(CartItemEntity(alice, 4, 2, Instant.now()))

        val orderId = orderDao.placeOrder(order(alice, total = 700), listOf(line(4, 2, price = 350)))

        val saved = orderDao.getOrder(orderId)!!
        assertEquals(700, saved.order.totalPence)
        assertEquals(1, saved.items.size)
        assertEquals(orderId, saved.items.single().orderId)
        assertTrue(db.cartDao().getCart(alice).isEmpty())
    }

    @Test
    fun customersOnlySeeTheirOwnOrdersNewestFirst() = runTest {
        val older = orderDao.placeOrder(order(alice, createdAt = 1_000), listOf(line(4, 1)))
        val newer = orderDao.placeOrder(order(alice, createdAt = 2_000), listOf(line(16, 1)))
        orderDao.placeOrder(order(bob), listOf(line(1, 1)))

        val ids = orderDao.observeOrdersForUser(alice).first().map { it.order.id }

        assertEquals(listOf(newer, older), ids)
    }

    @Test
    fun statusUpdatesWhenTheExpectedStatusMatches() = runTest {
        val id = orderDao.placeOrder(order(alice), listOf(line(4, 1)))

        val rows = orderDao.updateStatus(id, OrderStatus.PENDING, OrderStatus.PREPARING, Instant.now())

        assertEquals(1, rows)
        assertEquals(OrderStatus.PREPARING, orderDao.getOrder(id)!!.order.status)
    }

    @Test
    fun staleStatusUpdateChangesNothing() = runTest {
        val id = orderDao.placeOrder(order(alice), listOf(line(4, 1)))
        orderDao.updateStatus(id, OrderStatus.PENDING, OrderStatus.PREPARING, Instant.now())

        // A second update that still thinks the order is PENDING must not apply.
        val rows = orderDao.updateStatus(id, OrderStatus.PENDING, OrderStatus.PREPARING, Instant.now())

        assertEquals(0, rows)
    }

    @Test
    fun itemCountsAreSummedPerUserAndOverall() = runTest {
        orderDao.placeOrder(order(alice), listOf(line(4, 2), line(16, 1)))
        orderDao.placeOrder(order(alice), listOf(line(4, 1)))
        orderDao.placeOrder(order(bob), listOf(line(4, 5)))

        val aliceCounts = orderDao.getItemCountsForUser(alice).associate { it.menuItemId to it.total }
        val allCounts = orderDao.getItemCountsForAllUsers().associate { it.menuItemId to it.total }

        assertEquals(mapOf(4L to 3, 16L to 1), aliceCounts)
        assertEquals(mapOf(4L to 8, 16L to 1), allCounts)
    }

    @Test
    fun deletingAUserRemovesTheirOrdersAndLines() = runTest {
        val id = orderDao.placeOrder(order(alice), listOf(line(4, 1)))

        db.userDao().deleteById(alice)

        assertNull(orderDao.getOrder(id))
        assertTrue(orderDao.getItemCountsForAllUsers().isEmpty())
    }
}
