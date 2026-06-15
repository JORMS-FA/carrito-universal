package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllProducts(userId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE userId = :userId AND category = :category ORDER BY id DESC")
    fun getProductsByCategory(userId: String, category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE userId = :userId AND category = :category ORDER BY id DESC")
    suspend fun getProductsByCategorySync(userId: String, category: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT DISTINCT category FROM products WHERE userId = :userId")
    fun getUniqueCategories(userId: String): Flow<List<String>>

    @Query("SELECT * FROM products WHERE userId = :userId AND reminderDate IS NOT NULL AND status NOT IN ('Comprado', 'Descartado') ORDER BY reminderDate ASC")
    fun getProductsWithReminders(userId: String): Flow<List<ProductEntity>>
}
