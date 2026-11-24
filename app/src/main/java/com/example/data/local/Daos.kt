package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllOnce(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("UPDATE products SET stock = MAX(0, stock + :delta), updatedAt = :now, isSynced = 0 WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE timestamp >= :startOfDayMillis")
    suspend fun countToday(startOfDayMillis: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY (totalDebt - paidDebt) DESC, lastUpdated DESC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY (totalDebt - paidDebt) DESC, lastUpdated DESC")
    suspend fun getAllOnce(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): CustomerEntity?

    @Query("SELECT * FROM customers WHERE isSynced = 0")
    suspend fun getUnsynced(): List<CustomerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("UPDATE customers SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()
}

@Dao
interface KasbonEntryDao {
    @Query("SELECT * FROM kasbon_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<KasbonEntryEntity>>

    @Query("SELECT * FROM kasbon_entries WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun observeForCustomer(customerId: Long): Flow<List<KasbonEntryEntity>>

    @Query("SELECT * FROM kasbon_entries WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsynced(): List<KasbonEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: KasbonEntryEntity): Long

    @Query("UPDATE kasbon_entries SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllSynced()
}
