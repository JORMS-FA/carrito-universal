package com.example.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProductDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.productDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getProductsWithReminders_excludesBoughtAndDiscardedItems() = runBlocking {
        val now = System.currentTimeMillis()
        val userId = "user-1"

        dao.insertProduct(
            product = ProductEntity(
                userId = userId,
                title = "Pendiente",
                url = "https://example.com/pending",
                brand = "Brand",
                imageUrl = "https://example.com/pending.png",
                price = 100.0,
                currency = "USD",
                category = "Tech",
                priority = "Alta",
                status = "Prioritario",
                reminderDate = now + 1_000,
                rating = 4.5f,
                summaryMd = "# Pending",
                notes = "",
                sourceStore = "Web",
                comparisonGroupId = null
            )
        )
        dao.insertProduct(
            product = ProductEntity(
                userId = userId,
                title = "Comprado",
                url = "https://example.com/bought",
                brand = "Brand",
                imageUrl = "https://example.com/bought.png",
                price = 200.0,
                currency = "USD",
                category = "Tech",
                priority = "Alta",
                status = "Comprado",
                reminderDate = now + 2_000,
                rating = 4.5f,
                summaryMd = "# Bought",
                notes = "",
                sourceStore = "Web",
                comparisonGroupId = null
            )
        )
        dao.insertProduct(
            product = ProductEntity(
                userId = userId,
                title = "Descartado",
                url = "https://example.com/discarded",
                brand = "Brand",
                imageUrl = "https://example.com/discarded.png",
                price = 300.0,
                currency = "USD",
                category = "Tech",
                priority = "Alta",
                status = "Descartado",
                reminderDate = now + 3_000,
                rating = 4.5f,
                summaryMd = "# Discarded",
                notes = "",
                sourceStore = "Web",
                comparisonGroupId = null
            )
        )
        dao.insertProduct(
            product = ProductEntity(
                userId = userId,
                title = "Sin recordatorio",
                url = "https://example.com/no-reminder",
                brand = "Brand",
                imageUrl = "https://example.com/no-reminder.png",
                price = 400.0,
                currency = "USD",
                category = "Tech",
                priority = "Alta",
                status = "Prioritario",
                reminderDate = null,
                rating = 4.5f,
                summaryMd = "# No reminder",
                notes = "",
                sourceStore = "Web",
                comparisonGroupId = null
            )
        )

        val reminders = dao.getProductsWithReminders(userId).first()

        assertEquals(1, reminders.size)
        assertEquals("Pendiente", reminders.first().title)
        assertTrue(reminders.all { it.status != "Comprado" && it.status != "Descartado" })
    }
}
