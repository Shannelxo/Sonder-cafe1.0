package com.sondercafe.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {

    @Test
    fun `statuses follow the workflow in order`() {
        assertEquals(OrderStatus.PREPARING, OrderStatus.PENDING.next())
        assertEquals(OrderStatus.READY_FOR_COLLECTION, OrderStatus.PREPARING.next())
        assertEquals(OrderStatus.COLLECTED, OrderStatus.READY_FOR_COLLECTION.next())
    }

    @Test
    fun `collected is the final status`() {
        assertNull(OrderStatus.COLLECTED.next())
        assertFalse(OrderStatus.COLLECTED.isActive)
    }

    @Test
    fun `every status before collected is active`() {
        OrderStatus.entries
            .filter { it != OrderStatus.COLLECTED }
            .forEach { assertTrue("$it should be active", it.isActive) }
    }

    @Test
    fun `can only move forward one step`() {
        assertTrue(OrderStatus.PENDING.canMoveTo(OrderStatus.PREPARING))
        assertFalse(OrderStatus.PENDING.canMoveTo(OrderStatus.READY_FOR_COLLECTION))
        assertFalse(OrderStatus.PENDING.canMoveTo(OrderStatus.COLLECTED))
    }

    @Test
    fun `cannot move backwards or stay the same`() {
        assertFalse(OrderStatus.PREPARING.canMoveTo(OrderStatus.PENDING))
        assertFalse(OrderStatus.COLLECTED.canMoveTo(OrderStatus.READY_FOR_COLLECTION))
        assertFalse(OrderStatus.PREPARING.canMoveTo(OrderStatus.PREPARING))
    }
}
