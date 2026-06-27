package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.models.Feedback
import com.example.data.models.MenuItem
import com.example.data.models.Order
import com.example.data.models.Reservation
import com.example.data.models.CartItem
import com.example.data.repository.RestaurantRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationAlert(
    val type: String, // "PUSH", "SMS", "EMAIL"
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RestaurantRepository
    
    // UI states reflecting SQLite Room DB
    val menuItems: StateFlow<List<MenuItem>>
    val orders: StateFlow<List<Order>>
    val reservations: StateFlow<List<Reservation>>
    val feedbacks: StateFlow<List<Feedback>>

    // Cart state
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    // Tracking state
    private val _currentTrackingOrderId = MutableStateFlow<Int?>(null)
    val currentTrackingOrderId = _currentTrackingOrderId.asStateFlow()

    // Authentication sessions
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail = _currentUserEmail.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin = _isAdmin.asStateFlow()

    // Simulated Real-time Notifications/Alerts List
    private val _notifications = MutableStateFlow<List<NotificationAlert>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Latest notification for Toast alerts
    private val _latestAlert = MutableSharedFlow<NotificationAlert>()
    val latestAlert: SharedFlow<NotificationAlert> = _latestAlert.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = RestaurantRepository(database.restaurantDao())
        
        menuItems = repository.menuItems.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        orders = repository.orders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        reservations = repository.reservations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        feedbacks = repository.feedbacks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed DB on start
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Setup real-time listeners for order tracking status updates to fire notification events
        viewModelScope.launch {
            combine(orders, _currentTrackingOrderId) { orderList, trackingId ->
                if (trackingId != null) {
                    orderList.find { it.id == trackingId }
                } else {
                    null
                }
            }.collect { activeOrder ->
                if (activeOrder != null) {
                    onTrackedOrderStatusUpdated(activeOrder)
                }
            }
        }
    }

    // Keep track of last seen status to avoid duplicate notifications
    private val lastSeenStatuses = mutableMapOf<Int, String>()

    private fun onTrackedOrderStatusUpdated(order: Order) {
        val prevStatus = lastSeenStatuses[order.id]
        if (prevStatus != order.status) {
            lastSeenStatuses[order.id] = order.status
            
            // Generate notification alerts based on new status
            when (order.status) {
                "Pending" -> {
                    addAlert(
                        NotificationAlert(
                            type = "SMS",
                            title = "Order Placed Successfully",
                            message = "[SMS] Dear ${order.customerName}, your order #${order.id} for ₹${order.totalAmount} at The Empire has been placed. Tracking: ${order.orderType} mode."
                        )
                    )
                }
                "Preparing" -> {
                    addAlert(
                        NotificationAlert(
                            type = "PUSH",
                            title = "Chef is Preparing Your Food",
                            message = "[Push] The Empire: Order #${order.id} is now in the kitchen! Our master chefs are preparing your delicious dishes with fresh ingredients."
                        )
                    )
                }
                "Out for Delivery" -> {
                    addAlert(
                        NotificationAlert(
                            type = "SMS",
                            title = "Order Dispatched",
                            message = "[SMS] The Empire: Your order #${order.id} is out for delivery! Our delivery executive is heading your way. Please keep your phone reachable."
                        )
                    )
                    addAlert(
                        NotificationAlert(
                            type = "EMAIL",
                            title = "Shipping Status Update",
                            message = "[Email] To: ${order.customerEmail}\nSubject: Your Order #${order.id} is Out for Delivery!\n\nHi ${order.customerName},\nYour delicious meal is freshly packed and dispatched from The Empire. It will reach your doorstep shortly!"
                        )
                    )
                }
                "Delivered" -> {
                    addAlert(
                        NotificationAlert(
                            type = "PUSH",
                            title = "Order Delivered",
                            message = "[Push] The Empire: Order #${order.id} has been delivered successfully. Enjoy your meal and please share your feedback!"
                        )
                    )
                    addAlert(
                        NotificationAlert(
                            type = "EMAIL",
                            title = "Order Delivered Invoice",
                            message = "[Email] To: ${order.customerEmail}\nSubject: Delivered & Invoice for Order #${order.id}\n\nHi ${order.customerName},\nThank you for dining with The Empire! Your order #${order.id} has been delivered. Total amount of ₹${order.totalAmount} was paid via ${order.paymentMethod}."
                        )
                    )
                }
            }
        }
    }

    private fun addAlert(alert: NotificationAlert) {
        _notifications.value = listOf(alert) + _notifications.value
        viewModelScope.launch {
            _latestAlert.emit(alert)
        }
    }

    // --- CART ACTIONS ---
    fun addToCart(item: MenuItem) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItemId == item.id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentList.add(CartItem(menuItemId = item.id, name = item.name, price = item.price, quantity = 1))
        }
        _cart.value = currentList
    }

    fun removeFromCart(menuItemId: Int) {
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItemId == menuItemId }
        if (index >= 0) {
            currentList.removeAt(index)
        }
        _cart.value = currentList
    }

    fun updateCartQuantity(menuItemId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(menuItemId)
            return
        }
        val currentList = _cart.value.toMutableList()
        val index = currentList.indexOfFirst { it.menuItemId == menuItemId }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = quantity)
        }
        _cart.value = currentList
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.price * it.quantity }
    }

    // --- CUSTOMER ACTIONS ---
    fun placeOrder(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        deliveryAddress: String,
        paymentMethod: String,
        orderType: String,
        tableNumber: String = "",
        onSuccess: (Int) -> Unit
    ) {
        val itemsList = _cart.value
        if (itemsList.isEmpty()) return

        val itemsJson = repository.serializeCart(itemsList)
        val totalAmount = getCartTotal()

        val newOrder = Order(
            itemsJson = itemsJson,
            totalAmount = totalAmount,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            deliveryAddress = if (orderType == "Delivery") deliveryAddress else "Dine-in at Table $tableNumber",
            paymentMethod = paymentMethod,
            status = "Pending",
            orderType = orderType,
            tableNumber = tableNumber
        )

        viewModelScope.launch {
            val orderId = repository.insertOrder(newOrder)
            lastSeenStatuses[orderId] = "Pending"
            
            // Fire Initial Placement Notification
            addAlert(
                NotificationAlert(
                    type = "SMS",
                    title = "Order Registered",
                    message = "[SMS] Dear $customerName, order #$orderId has been successfully placed at The Empire! Track live status inside the app. Total: ₹$totalAmount."
                )
            )
            addAlert(
                NotificationAlert(
                    type = "EMAIL",
                    title = "Order Received Invoice",
                    message = "[Email] To: $customerEmail\nSubject: Invoice & Details for Order #$orderId\n\nHi $customerName,\nWe have received your order #$orderId. Our kitchen is waiting to prepare your food!\n\nOrder Total: ₹$totalAmount\nPayment Method: $paymentMethod\nType: $orderType\nAddress/Table: ${newOrder.deliveryAddress}"
                )
            )

            _currentTrackingOrderId.value = orderId
            clearCart()
            onSuccess(orderId)
        }
    }

    fun bookTable(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        date: String,
        time: String,
        guests: Int,
        specialRequest: String,
        onSuccess: () -> Unit
    ) {
        val reservation = Reservation(
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            date = date,
            time = time,
            guests = guests,
            specialRequest = specialRequest,
            status = "Confirmed"
        )

        viewModelScope.launch {
            val resId = repository.insertReservation(reservation)
            // Fire reservation notification
            addAlert(
                NotificationAlert(
                    type = "EMAIL",
                    title = "Reservation Confirmed",
                    message = "[Email] To: $customerEmail\nSubject: Table Reservation Confirmed at The Empire! (#R-$resId)\n\nHi $customerName,\nYour table reservation for $guests guests on $date at $time is officially confirmed! Special request: '$specialRequest'. We look forward to serving you."
                )
            )
            addAlert(
                NotificationAlert(
                    type = "PUSH",
                    title = "Table Booked!",
                    message = "[Push] The Empire: Table booked for $guests guests on $date at $time. Reservation ID: #R-$resId"
                )
            )
            onSuccess()
        }
    }

    fun submitFeedback(
        customerName: String,
        customerEmail: String,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit
    ) {
        val feedback = Feedback(
            customerName = customerName,
            customerEmail = customerEmail,
            rating = rating,
            comment = comment
        )

        viewModelScope.launch {
            repository.insertFeedback(feedback)
            addAlert(
                NotificationAlert(
                    type = "PUSH",
                    title = "Feedback Submitted",
                    message = "[Push] Thank you $customerName! We appreciate your $rating-star rating."
                )
            )
            onSuccess()
        }
    }

    fun setTrackingOrderId(id: Int?) {
        _currentTrackingOrderId.value = id
    }

    // --- AUTHENTICATION ACTIONS ---
    fun loginAdmin(email: String, password: String): Boolean {
        if (email == "subhu07web@gmail.com" && password == "admin2026") {
            _currentUserEmail.value = email
            _isAdmin.value = true
            return true
        }
        return false
    }

    fun logout() {
        _currentUserEmail.value = null
        _isAdmin.value = false
    }

    // --- ADMIN MANAGEMENT ACTIONS ---
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            // Retrieve full order to issue alerts
            // Room flow will trigger combine listener automatically in a few millis
        }
    }

    fun deleteOrder(orderId: Int) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
        }
    }

    fun updateReservationStatus(resId: Int, status: String) {
        viewModelScope.launch {
            repository.updateReservationStatus(resId, status)
            // Send alert
            addAlert(
                NotificationAlert(
                    type = "PUSH",
                    title = "Reservation Updated",
                    message = "[Push] Reservation #R-$resId updated to '$status'"
                )
            )
        }
    }

    fun addNewMenuItem(
        name: String,
        description: String,
        price: Double,
        category: String,
        imageUrl: String,
        isPopular: Boolean
    ) {
        val item = MenuItem(
            name = name,
            description = description,
            price = price,
            category = category,
            imageUrl = if (imageUrl.trim().isEmpty()) "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&q=80&w=600" else imageUrl,
            isPopular = isPopular
        )
        viewModelScope.launch {
            repository.insertMenuItem(item)
        }
    }

    fun updateMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
        }
    }

    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            repository.deleteMenuItemById(id)
        }
    }
}
