package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.Feedback
import com.example.data.models.MenuItem
import com.example.data.models.Order
import com.example.data.models.Reservation
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // --- MENU ITEMS ---
    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItem>)

    @Update
    suspend fun updateMenuItem(item: MenuItem)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Int)

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun getMenuItemCount(): Int


    // --- ORDERS ---
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Int): Flow<Order?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Int, status: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Int)


    // --- RESERVATIONS ---
    @Query("SELECT * FROM reservations ORDER BY timestamp DESC")
    fun getAllReservations(): Flow<List<Reservation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: Reservation): Long

    @Query("UPDATE reservations SET status = :status WHERE id = :id")
    suspend fun updateReservationStatus(id: Int, status: String)


    // --- FEEDBACKS ---
    @Query("SELECT * FROM feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<Feedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: Feedback): Long
}
