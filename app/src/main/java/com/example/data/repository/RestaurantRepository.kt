package com.example.data.repository

import com.example.data.local.RestaurantDao
import com.example.data.models.Feedback
import com.example.data.models.MenuItem
import com.example.data.models.Order
import com.example.data.models.Reservation
import com.example.data.models.CartItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RestaurantRepository(private val dao: RestaurantDao) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val cartListType = Types.newParameterizedType(List::class.java, CartItem::class.java)
    private val cartAdapter = moshi.adapter<List<CartItem>>(cartListType)

    val menuItems: Flow<List<MenuItem>> = dao.getAllMenuItems()
    val orders: Flow<List<Order>> = dao.getAllOrders()
    val reservations: Flow<List<Reservation>> = dao.getAllReservations()
    val feedbacks: Flow<List<Feedback>> = dao.getAllFeedbacks()

    fun getOrderById(id: Int): Flow<Order?> = dao.getOrderById(id)

    // Helper serialization
    fun serializeCart(cart: List<CartItem>): String {
        return try {
            cartAdapter.toJson(cart) ?: "[]"
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeCart(json: String): List<CartItem> {
        return try {
            cartAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertOrder(order: Order): Int = withContext(Dispatchers.IO) {
        dao.insertOrder(order).toInt()
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, status)
    }

    suspend fun deleteOrder(orderId: Int) = withContext(Dispatchers.IO) {
        dao.deleteOrder(orderId)
    }

    suspend fun insertReservation(reservation: Reservation): Int = withContext(Dispatchers.IO) {
        dao.insertReservation(reservation).toInt()
    }

    suspend fun updateReservationStatus(reservationId: Int, status: String) = withContext(Dispatchers.IO) {
        dao.updateReservationStatus(reservationId, status)
    }

    suspend fun insertFeedback(feedback: Feedback) = withContext(Dispatchers.IO) {
        dao.insertFeedback(feedback)
    }

    suspend fun insertMenuItem(item: MenuItem) = withContext(Dispatchers.IO) {
        dao.insertMenuItem(item)
    }

    suspend fun updateMenuItem(item: MenuItem) = withContext(Dispatchers.IO) {
        dao.updateMenuItem(item)
    }

    suspend fun deleteMenuItemById(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteMenuItemById(id)
    }

    // Database seeding routine
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getMenuItemCount() == 0) {
            val initialItems = listOf(
                MenuItem(
                    name = "Paneer Tikka Angare",
                    description = "Succulent cottage cheese cubes marinated in aromatic spices and grilled in a clay oven with charcoal.",
                    price = 280.0,
                    category = "Starters",
                    imageUrl = "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                ),
                MenuItem(
                    name = "Empire Crispy Spring Rolls",
                    description = "Golden crispy rolls stuffed with exotic shredded vegetables and glass noodles, served with spicy dip.",
                    price = 190.0,
                    category = "Starters",
                    imageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&q=80&w=600",
                    isPopular = false
                ),
                MenuItem(
                    name = "Tandoori Chicken Kebab Platter",
                    description = "An assortment of sizzling chicken seekh, tikka, and malai kebabs served with fresh mint chutney.",
                    price = 390.0,
                    category = "Starters",
                    imageUrl = "https://images.unsplash.com/photo-1626132647523-66f5bf380027?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                ),
                MenuItem(
                    name = "Royal Empire Butter Chicken",
                    description = "Slices of tandoori grilled chicken slowly cooked in a rich, velvety tomato gravy loaded with butter and cream.",
                    price = 450.0,
                    category = "Mains",
                    imageUrl = "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                ),
                MenuItem(
                    name = "Deccani Chicken Biryani",
                    description = "Fragrant long-grain basmati rice layered with cooked spiced tender chicken, saffron, and fresh mint in traditional Dum style.",
                    price = 420.0,
                    category = "Mains",
                    imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                ),
                MenuItem(
                    name = "Paneer Lababdar",
                    description = "Fresh cottage cheese in a rich, chunky onion-tomato cream sauce, served with traditional spices.",
                    price = 340.0,
                    category = "Mains",
                    imageUrl = "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&q=80&w=600",
                    isPopular = false
                ),
                MenuItem(
                    name = "Wild Mushroom & Truffle Risotto",
                    description = "Premium Italian arborio rice cooked to creamy perfection with wild forest mushrooms, parmesan cheese, and black truffle oil.",
                    price = 380.0,
                    category = "Mains",
                    imageUrl = "https://images.unsplash.com/photo-1476124369491-e7addf5db371?auto=format&fit=crop&q=80&w=600",
                    isPopular = false
                ),
                MenuItem(
                    name = "Sizzling Chocolate Fudge Brownie",
                    description = "Warm chocolate fudge brownie loaded with walnuts, served sizzling on a iron plate with premium vanilla ice cream.",
                    price = 240.0,
                    category = "Desserts",
                    imageUrl = "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                ),
                MenuItem(
                    name = "Gulab Jamun with Kesar Rabri",
                    description = "Saffron infused golden-fried milk-solid dumplings dipped in warm cardamom sugar syrup, topped with thick condensed milk.",
                    price = 180.0,
                    category = "Desserts",
                    imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&q=80&w=600",
                    isPopular = false
                ),
                MenuItem(
                    name = "Fresh Mint & Basil Mojito",
                    description = "A bubbly blend of muddled fresh mint, fragrant sweet basil, lime juice, brown sugar, topped with chilled soda.",
                    price = 150.0,
                    category = "Beverages",
                    imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&q=80&w=600",
                    isPopular = false
                ),
                MenuItem(
                    name = "Royal Mango Lassi",
                    description = "A sweet, creamy traditional yogurt shake infused with pure Alphonso mango pulp, saffron, and slivered pistachios.",
                    price = 130.0,
                    category = "Beverages",
                    imageUrl = "https://images.unsplash.com/photo-1553530666-ba11a7da3888?auto=format&fit=crop&q=80&w=600",
                    isPopular = true
                )
            )
            dao.insertMenuItems(initialItems)
        }
    }
}
