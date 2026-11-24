package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        TransactionEntity::class,
        CustomerEntity::class,
        KasbonEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WarungDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao
    abstract fun customerDao(): CustomerDao
    abstract fun kasbonEntryDao(): KasbonEntryDao

    companion object {
        @Volatile
        private var INSTANCE: WarungDatabase? = null

        fun getInstance(context: Context): WarungDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarungDatabase::class.java,
                    "warungkasir_offline.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        val INITIAL_SEED_PRODUCTS: List<ProductEntity> = listOf(
            ProductEntity(
                name = "Beras 5kg",
                barcode = "8991002100011",
                category = "Sembako",
                costPrice = 62000L,
                sellPrice = 72000L,
                stock = 14,
                minStock = 5,
                isSynced = true
            ),
            ProductEntity(
                name = "Minyak Goreng 1L",
                barcode = "8991002100028",
                category = "Sembako",
                costPrice = 15500L,
                sellPrice = 18500L,
                stock = 4,
                minStock = 6,
                isSynced = true
            ),
            ProductEntity(
                name = "Gula Pasir 1kg",
                barcode = "8991002100035",
                category = "Sembako",
                costPrice = 14500L,
                sellPrice = 17500L,
                stock = 18,
                minStock = 5,
                isSynced = true
            ),
            ProductEntity(
                name = "Telur Ayam 1kg",
                barcode = "8991002100042",
                category = "Sembako",
                costPrice = 25000L,
                sellPrice = 29000L,
                stock = 3,
                minStock = 5,
                isSynced = true
            ),
            ProductEntity(
                name = "Mie Instan",
                barcode = "8991002100059",
                category = "Makanan",
                costPrice = 2600L,
                sellPrice = 3500L,
                stock = 42,
                minStock = 12,
                isSynced = true
            ),
            ProductEntity(
                name = "Kopi Sachet",
                barcode = "8991002100066",
                category = "Minuman",
                costPrice = 1300L,
                sellPrice = 2000L,
                stock = 28,
                minStock = 10,
                isSynced = true
            ),
            ProductEntity(
                name = "Teh Celup",
                barcode = "8991002100073",
                category = "Minuman",
                costPrice = 5200L,
                sellPrice = 7000L,
                stock = 4,
                minStock = 5,
                isSynced = true
            ),
            ProductEntity(
                name = "Sabun Cuci",
                barcode = "8991002100080",
                category = "Kebersihan",
                costPrice = 11000L,
                sellPrice = 14000L,
                stock = 12,
                minStock = 4,
                isSynced = true
            )
        )
    }
}
