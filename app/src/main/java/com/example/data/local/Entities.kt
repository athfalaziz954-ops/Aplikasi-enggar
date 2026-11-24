package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val barcode: String,
    val category: String,
    val costPrice: Long,
    val sellPrice: Long,
    val stock: Int,
    val minStock: Int,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val isLowStock: Boolean
        get() = stock <= minStock
}

data class CartItemPayload(
    val productId: Long,
    val name: String,
    val barcode: String,
    val costPrice: Long,
    val sellPrice: Long,
    val quantity: Int
) {
    val subtotal: Long get() = sellPrice * quantity
    val profit: Long get() = (sellPrice - costPrice) * quantity

    fun toJson(): JSONObject = JSONObject().apply {
        put("productId", productId)
        put("name", name)
        put("barcode", barcode)
        put("costPrice", costPrice)
        put("sellPrice", sellPrice)
        put("quantity", quantity)
    }

    companion object {
        fun listToJsonString(items: List<CartItemPayload>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJson()) }
            return array.toString()
        }

        fun listFromJsonString(json: String): List<CartItemPayload> {
            return try {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    CartItemPayload(
                        productId = obj.optLong("productId", 0L),
                        name = obj.optString("name", ""),
                        barcode = obj.optString("barcode", ""),
                        costPrice = obj.optLong("costPrice", 0L),
                        sellPrice = obj.optLong("sellPrice", 0L),
                        quantity = obj.optInt("quantity", 1)
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerId: Long? = null,
    val customerName: String = "Pelanggan Umum",
    val paymentMethod: String, // "TUNAI" or "KASBON"
    val totalAmount: Long,
    val paidAmount: Long,
    val changeOrRemaining: Long,
    val itemsJson: String,
    val isSynced: Boolean = false
) {
    val items: List<CartItemPayload>
        get() = CartItemPayload.listFromJsonString(itemsJson)

    val estimatedProfit: Long
        get() = items.sumOf { it.profit }
}

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val whatsapp: String,
    val totalDebt: Long = 0L,
    val paidDebt: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val remainingDebt: Long
        get() = (totalDebt - paidDebt).coerceAtLeast(0L)

    val repaymentProgress: Float
        get() = if (totalDebt <= 0L) 1f else (paidDebt.toFloat() / totalDebt.toFloat()).coerceIn(0f, 1f)
}

@Entity(tableName = "kasbon_entries")
data class KasbonEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String, // "UTANG" or "CICILAN"
    val amount: Long,
    val note: String,
    val invoiceNumber: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
