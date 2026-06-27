package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Starters", "Mains", "Desserts", "Beverages"
    val imageUrl: String,
    val isPopular: Boolean = false,
    val isAvailable: Boolean = true
)

@JsonClass(generateAdapter = true)
data class CartItem(
    val menuItemId: Int,
    val name: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemsJson: String, // Serialized List<CartItem>
    val totalAmount: Double,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val paymentMethod: String, // "Card", "UPI", "Cash on Delivery"
    val status: String, // "Pending", "Preparing", "Out for Delivery", "Delivered"
    val timestamp: Long = System.currentTimeMillis(),
    val orderType: String, // "Delivery" or "Dine-in"
    val tableNumber: String = ""
)

@Entity(tableName = "reservations")
data class Reservation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val date: String,
    val time: String,
    val guests: Int,
    val specialRequest: String = "",
    val status: String = "Confirmed", // "Confirmed", "Completed", "Cancelled"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "feedbacks")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerEmail: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
