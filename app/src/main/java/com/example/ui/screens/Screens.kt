package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.models.CartItem
import com.example.data.models.Feedback
import com.example.data.models.MenuItem
import com.example.data.models.Order
import com.example.data.models.Reservation
import com.example.ui.theme.*
import com.example.ui.viewmodel.NotificationAlert
import com.example.ui.viewmodel.RestaurantViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Menu : Screen("menu", "Menu", Icons.Default.RestaurantMenu)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object BookTable : Screen("book_table", "Book Table", Icons.Default.TableBar)
    object Tracker : Screen("tracker", "Track Order", Icons.Default.DirectionsRun)
    object Feedback : Screen("feedback", "Feedback", Icons.Default.RateReview)
    object Admin : Screen("admin", "Admin", Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: RestaurantViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val activeTrackingId by viewModel.currentTrackingOrderId.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    
    // Listen to real-time status updates/alerts and show a professional native toast
    LaunchedEffect(key1 = true) {
        viewModel.latestAlert.collectLatest { alert ->
            Toast.makeText(context, "${alert.title}: ${alert.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Responsive: Detect screen size
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = GoldAccent
                        )
                        Text(
                            text = "THE EMPIRE",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = GoldPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    // Quick view for active alerts count / Notification history dropdown
                    var showNotificationDialog by remember { mutableStateOf(false) }
                    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = { showNotificationDialog = true }) {
                            BadgedBox(badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = GoldAccent) {
                                        Text(text = notifications.size.toString(), color = Color.Black)
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Alerts Hub",
                                    tint = GoldAccent
                                )
                            }
                        }
                    }

                    if (showNotificationDialog) {
                        NotificationHubDialog(
                            notifications = notifications,
                            onDismiss = { showNotificationDialog = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = GoldPrimary
                )
            )
        },
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    val navigationItems = listOf(
                        Screen.Home,
                        Screen.Menu,
                        Screen.Cart,
                        Screen.BookTable,
                        Screen.Tracker,
                        Screen.Feedback,
                        Screen.Admin
                    )

                    navigationItems.forEach { screen ->
                        val selected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            icon = {
                                if (screen == Screen.Cart && cart.isNotEmpty()) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = GoldAccent) {
                                            Text(
                                                text = cart.sumOf { it.quantity }.toString(),
                                                color = Color.Black
                                            )
                                        }
                                    }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else if (screen == Screen.Tracker && activeTrackingId != null) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = OrderStatusGreen) {
                                            Text("•", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = GoldAccent,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            ) {
                // Call Floating Button
                FloatingActionButton(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:+918822344281")
                        }
                        context.startActivity(intent)
                    },
                    containerColor = GoldPrimary,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("floating_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Us",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // WhatsApp Floating Button
                FloatingActionButton(
                    onClick = {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=918822344281&text=Hello%20The%20Empire!%20I%20would%20like%20to%20inquire%20about%20ordering.")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp detail error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = Color(0xFF25D366),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("floating_whatsapp_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Us",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // If wide screen, show left navigation rail instead of bottom bar
            if (isWideScreen) {
                NavigationRail(
                    containerColor = DarkSurface,
                    header = {
                        Icon(
                            imageVector = Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    val railItems = listOf(
                        Screen.Home,
                        Screen.Menu,
                        Screen.Cart,
                        Screen.BookTable,
                        Screen.Tracker,
                        Screen.Feedback,
                        Screen.Admin
                    )

                    railItems.forEach { screen ->
                        val selected = currentScreen.route == screen.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            icon = {
                                if (screen == Screen.Cart && cart.isNotEmpty()) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = GoldAccent) {
                                            Text(
                                                text = cart.sumOf { it.quantity }.toString(),
                                                color = Color.Black
                                            )
                                        }
                                    }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else if (screen == Screen.Tracker && activeTrackingId != null) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = OrderStatusGreen) {
                                            Text("•", color = Color.White)
                                        }
                                    }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = GoldAccent,
                                indicatorColor = GoldPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Main screen content area (with transition animations)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(DarkBg)
            ) {
                when (currentScreen) {
                    is Screen.Home -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToMenu = { currentScreen = Screen.Menu },
                        onNavigateToBooking = { currentScreen = Screen.BookTable }
                    )
                    is Screen.Menu -> MenuScreen(viewModel = viewModel)
                    is Screen.Cart -> CartScreen(
                        viewModel = viewModel,
                        onNavigateToTracker = { currentScreen = Screen.Tracker }
                    )
                    is Screen.BookTable -> BookTableScreen(viewModel = viewModel)
                    is Screen.Tracker -> TrackerScreen(viewModel = viewModel)
                    is Screen.Feedback -> FeedbackScreen(viewModel = viewModel)
                    is Screen.Admin -> AdminScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// --- SCREEN 1: HOMEPAGE ---
@Composable
fun HomeScreen(
    viewModel: RestaurantViewModel,
    onNavigateToMenu: () -> Unit,
    onNavigateToBooking: () -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val popularDishes = remember(menuItems) { menuItems.filter { it.isPopular } }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Brand Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
            ) {
                // Background Premium Food Banner (Loaded via painterResource from local drawable)
                Image(
                    painter = painterResource(id = com.example.R.drawable.restaurant_hero),
                    contentDescription = "The Empire Royal Cuisine Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "THE EMPIRE",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Experience Royal Indian Fine Dining & Seamless Doorstep Delivery",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }

        // Fast CTAs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onNavigateToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("order_now_home_button"),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Order Now", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToBooking,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .testTag("book_table_home_button")
                ) {
                    Icon(Icons.Default.TableBar, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Book Table", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Popular Dishes Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Most Popular Dishes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Text(
                    text = "See All",
                    color = GoldAccent,
                    modifier = Modifier.clickable { onNavigateToMenu() },
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Popular Dishes Horizontal Scroll
        item {
            if (popularDishes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularDishes) { item ->
                        PopularDishCard(item = item, onAddClick = {
                            viewModel.addToCart(item)
                            Toast.makeText(context, "${item.name} added to cart!", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        }

        // Royal Ambiance / Offer Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Royal Dining Experience",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enjoy an exclusive 15% discount on advance table reservations or online deliveries this week. Use Coupon: EMPIRE15",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "Promo",
                        tint = GoldPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PopularDishCard(item: MenuItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(GoldPrimary, CircleShape)
                            .testTag("add_popular_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: INTERACTIVE MENU ---
@Composable
fun MenuScreen(viewModel: RestaurantViewModel) {
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Starters", "Mains", "Desserts", "Beverages")

    val filteredItems = remember(menuItems, searchQuery, selectedCategory) {
        menuItems.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || 
                    item.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    // Adapt grid based on screen width (Mobile vs Tablet/Desktop)
    val configuration = LocalConfiguration.current
    val gridColumns = when {
        configuration.screenWidthDp >= 900 -> 3
        configuration.screenWidthDp >= 600 -> 2
        else -> 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search delicious foods...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = GoldDark,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("menu_search_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Categories Scroll Tab Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                val bgColor = if (isSelected) GoldPrimary else DarkSurface
                val textColor = if (isSelected) Color.Black else TextSecondary
                val strokeColor = if (isSelected) GoldPrimary else GoldDark

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgColor)
                        .border(1.dp, strokeColor, RoundedCornerShape(20.dp))
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("category_tab_$category")
                ) {
                    Text(
                        text = category,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food Menu Grid
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No delicious items found!",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredItems) { item ->
                    MenuListItem(item = item, onAddClick = {
                        viewModel.addToCart(item)
                        Toast.makeText(context, "${item.name} added to cart!", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

@Composable
fun MenuListItem(item: MenuItem, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isPopular) {
                        Box(
                            modifier = Modifier
                                .background(GoldPrimary, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "POPULAR",
                                fontSize = 8.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("add_menu_item_${item.id}")
                    ) {
                        Text("Add", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: CART & PAYMENT CHECKOUT ---
@Composable
fun CartScreen(viewModel: RestaurantViewModel, onNavigateToTracker: () -> Unit) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showCheckoutForm by remember { mutableStateOf(false) }
    var showPaymentGateway by remember { mutableStateOf(false) }

    // Checkout Details
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var orderType by remember { mutableStateOf("Delivery") } // "Delivery" or "Dine-in"
    var tableNumber by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Card") } // "Card" or "UPI"

    if (showPaymentGateway) {
        PaymentGatewayScreen(
            totalAmount = viewModel.getCartTotal(),
            onPaymentSuccess = {
                // Submit order to DB
                viewModel.placeOrder(
                    customerName = name,
                    customerEmail = email,
                    customerPhone = phone,
                    deliveryAddress = address,
                    paymentMethod = paymentMethod,
                    orderType = orderType,
                    tableNumber = tableNumber,
                    onSuccess = { orderId ->
                        showPaymentGateway = false
                        showCheckoutForm = false
                        onNavigateToTracker()
                    }
                )
            },
            onCancel = { showPaymentGateway = false }
        )
        return
    }

    if (showCheckoutForm) {
        // Checkout Details Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { showCheckoutForm = false }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                }
                Text("Checkout Details", style = MaterialTheme.typography.titleLarge, color = GoldPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Order Type Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { orderType = "Delivery" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (orderType == "Delivery") GoldPrimary else DarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (orderType == "Delivery") Color.Black else GoldAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Delivery",
                                color = if (orderType == "Delivery") Color.Black else GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { orderType = "Dine-in" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (orderType == "Dine-in") GoldPrimary else DarkSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.TableBar,
                                contentDescription = null,
                                tint = if (orderType == "Dine-in") Color.Black else GoldAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Dine-in Table",
                                color = if (orderType == "Dine-in") Color.Black else GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Full Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address (for real receipt updates)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_email_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Mobile Number (for SMS Alerts)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input")
                    )
                }

                if (orderType == "Delivery") {
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Complete Delivery Address", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("checkout_address_input")
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = tableNumber,
                            onValueChange = { tableNumber = it },
                            label = { Text("Select Table Number (e.g. 5, 12, Window Seating)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldDark
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_table_input")
                        )
                    }
                }

                item {
                    // Payment Option Row
                    Text("Select Payment Gateway", color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (paymentMethod == "Card") GoldDark.copy(alpha = 0.3f) else DarkSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (paymentMethod == "Card") GoldPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { paymentMethod = "Card" }
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Credit/Debit Card", fontSize = 12.sp, color = TextPrimary)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (paymentMethod == "UPI") GoldDark.copy(alpha = 0.3f) else DarkSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (paymentMethod == "UPI") GoldPrimary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { paymentMethod = "UPI" }
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.QrCode, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("UPI Payments", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.trim().isEmpty() || email.trim().isEmpty() || phone.trim().isEmpty() ||
                        (orderType == "Delivery" && address.trim().isEmpty()) ||
                        (orderType == "Dine-in" && tableNumber.trim().isEmpty())
                    ) {
                        Toast.makeText(context, "Please complete all fields to proceed", Toast.LENGTH_SHORT).show()
                    } else {
                        showPaymentGateway = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("checkout_proceed_button")
            ) {
                Text(
                    text = "Proceed to Secure Pay (₹${viewModel.getCartTotal().toInt()})",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        return
    }

    // Default Cart List
    if (cart.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.RemoveShoppingCart,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your shopping cart is empty!", color = TextSecondary, fontSize = 16.sp)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Your Selected Dishes", style = MaterialTheme.typography.titleLarge, color = GoldPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cart) { item ->
                    CartListItem(
                        item = item,
                        onAdd = { viewModel.updateCartQuantity(item.menuItemId, item.quantity + 1) },
                        onMinus = { viewModel.updateCartQuantity(item.menuItemId, item.quantity - 1) },
                        onDelete = { viewModel.removeFromCart(item.menuItemId) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bills Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", color = TextSecondary)
                        Text("₹${viewModel.getCartTotal().toInt()}", color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taxes (GST 5%)", color = TextSecondary)
                        val tax = (viewModel.getCartTotal() * 0.05).toInt()
                        Text("₹$tax", color = TextPrimary)
                    }
                    Divider(color = GoldDark, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Bill", color = GoldAccent, fontWeight = FontWeight.Bold)
                        val total = (viewModel.getCartTotal() * 1.05).toInt()
                        Text("₹$total", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showCheckoutForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("checkout_button")
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checkout Order", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CartListItem(item: CartItem, onAdd: () -> Unit, onMinus: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text("₹${item.price.toInt()} per unit", color = GoldAccent, fontSize = 12.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMinus) {
                    Icon(Icons.Default.Remove, contentDescription = "Minus", tint = GoldAccent)
                }
                Text(
                    item.quantity.toString(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = GoldAccent)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

// --- SECURE PAYMENT GATEWAY POPUP SCREEN ---
@Composable
fun PaymentGatewayScreen(totalAmount: Double, onPaymentSuccess: () -> Unit, onCancel: () -> Unit) {
    var cardNumber by remember { mutableStateOf("4321567890123456") }
    var expiry by remember { mutableStateOf("12/29") }
    var cvv by remember { mutableStateOf("123") }
    var holder by remember { mutableStateOf("Royal Guest") }
    var isProcessing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isProcessing) {
            CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(60.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Contacting The Empire Payment Gateway...", color = GoldAccent, fontWeight = FontWeight.Bold)
            Text("Securing SSL/TLS 256-bit Connection...", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Taxes & Billing Amount: ₹${(totalAmount * 1.05).toInt()}", color = TextPrimary)
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SECURE CHECKOUT", color = GoldAccent, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = GoldPrimary)
                    }
                    Divider(color = GoldDark, modifier = Modifier.padding(vertical = 12.dp))

                    Text("Total Amount: ₹${(totalAmount * 1.05).toInt()}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { if (it.length <= 16) cardNumber = it },
                        label = { Text("16 Digit Card Number", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { if (it.length <= 5) expiry = it },
                            label = { Text("MM/YY", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldDark
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { if (it.length <= 3) cvv = it },
                            label = { Text("CVV", color = TextSecondary) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = holder,
                        onValueChange = { holder = it },
                        label = { Text("Card Holder Name", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isProcessing = true
                            scope.launch {
                                delay(1500) // Simulate Bank Verification Secure Lag
                                isProcessing = false
                                onPaymentSuccess()
                                Toast.makeText(context, "Payment Successful! Order Confirmed", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Pay Now", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel Transaction", color = Color.Red)
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: REAL-TIME TRACKER ---
@Composable
fun TrackerScreen(viewModel: RestaurantViewModel) {
    val activeTrackingId by viewModel.currentTrackingOrderId.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val activeOrder = remember(orders, activeTrackingId) {
        orders.find { it.id == activeTrackingId }
    }

    if (activeOrder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(70.dp)
                )
                Text("No active orders being tracked currently", color = TextSecondary)
                Text(
                    "You will automatically land here when you place a delicious meal order!",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Step calculations
    val status = activeOrder.status
    val stepIndex = when (status) {
        "Pending" -> 0
        "Preparing" -> 1
        "Out for Delivery" -> 2
        "Delivered" -> 3
        else -> 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Live Order Tracker - Order #${activeOrder.id}",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Beautiful Interactive Status Stepper Row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Estimated Delivery: 25-35 Minutes",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Stepper UI
                    val states = listOf("Placed", "Kitchen", "Dispatched", "Arrived")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        states.forEachIndexed { index, state ->
                            val isCompleted = stepIndex >= index
                            val color = if (isCompleted) GoldPrimary else TextSecondary
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(if (isCompleted) GoldPrimary else DarkSurfaceElevated, CircleShape)
                                        .border(1.dp, GoldDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (stepIndex > index) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            text = (index + 1).toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) Color.Black else TextSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(state, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Custom Live Drawing Map Canvas representation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw Road line
                        val pathPoints = listOf(
                            Offset(width * 0.15f, height * 0.5f),
                            Offset(width * 0.4f, height * 0.3f),
                            Offset(width * 0.65f, height * 0.7f),
                            Offset(width * 0.85f, height * 0.5f)
                        )

                        // Draw the full road path
                        for (i in 0 until pathPoints.size - 1) {
                            drawLine(
                                color = GoldDark.copy(alpha = 0.5f),
                                start = pathPoints[i],
                                end = pathPoints[i + 1],
                                strokeWidth = 14f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw Completed road path highlights
                        for (i in 0 until stepIndex) {
                            if (i < pathPoints.size - 1) {
                                drawLine(
                                    color = GoldAccent,
                                    start = pathPoints[i],
                                    end = pathPoints[i + 1],
                                    strokeWidth = 14f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Draw Icons/Dots at checkpoints
                        // Kitchen Base (The Empire)
                        drawCircle(
                            color = GoldPrimary,
                            radius = 20f,
                            center = pathPoints[0]
                        )
                        // Dest Base (Home)
                        drawCircle(
                            color = OrderStatusGreen,
                            radius = 20f,
                            center = pathPoints[3]
                        )

                        // Draw Delivery executive moving indicator
                        val riderPos = when (stepIndex) {
                            0 -> pathPoints[0]
                            1 -> pathPoints[1]
                            2 -> pathPoints[2]
                            3 -> pathPoints[3]
                            else -> pathPoints[0]
                        }
                        drawCircle(
                            color = Color.White,
                            radius = 28f,
                            center = riderPos
                        )
                        drawCircle(
                            color = GoldAccent,
                            radius = 16f,
                            center = riderPos
                        )
                    }

                    // Annotations overlays
                    Text(
                        "The Empire Kitchen",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp, top = 64.dp)
                    )

                    Text(
                        "Your Seating/Door",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp, top = 64.dp)
                    )
                }
            }
        }

        // Current status messages
        item {
            val descriptiveText = when (status) {
                "Pending" -> "Your order is placed successfully. Our manager is reviewing the items to dispatch to chefs!"
                "Preparing" -> "Master Chefs are heating up the wok! Sizzling delicious food smells are filling The Empire."
                "Out for Delivery" -> "Fresh food packed in heat-retaining containers. Our dispatch rider is driving smoothly towards you!"
                "Delivered" -> "Delivered safely! Please open the box, enjoy the rich aroma, and dine like royalty."
                else -> ""
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = GoldDark.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Status: $status", color = GoldAccent, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(descriptiveText, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

// --- SCREEN 5: ADVANCE TABLE BOOKING ---
@Composable
fun BookTableScreen(viewModel: RestaurantViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf("2") }
    var request by remember { mutableStateOf("") }

    var isBookedSuccessfully by remember { mutableStateOf(false) }

    val context = LocalContext.current

    if (isBookedSuccessfully) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = OrderStatusGreen,
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Table Booked Successfully!",
                style = MaterialTheme.typography.headlineSmall,
                color = GoldAccent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Dear $name, your Royal Seating request for $guests guests has been accepted on $date at $time.",
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "A secure confirmation email & SMS receipt has been simulated to $email.",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    isBookedSuccessfully = false
                    name = ""
                    email = ""
                    phone = ""
                    date = ""
                    time = ""
                    guests = "2"
                    request = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Book Another Table", color = Color.Black)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Book A Table in Advance",
                style = MaterialTheme.typography.titleLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Pre-arrange elegant seating for candle light, meetings, or family functions.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Guest Name", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_name_input")
            )
        }

        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_email_input")
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_phone_input")
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (DD/MM)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("booking_date_input")
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 8:30 PM)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("booking_time_input")
                )
            }
        }

        item {
            OutlinedTextField(
                value = guests,
                onValueChange = { guests = it },
                label = { Text("Number of Guests", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_guests_input")
            )
        }

        item {
            OutlinedTextField(
                value = request,
                onValueChange = { request = it },
                label = { Text("Special Requests (Candlelight, Birthday, etc.)", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = GoldDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("booking_request_input")
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || date.isEmpty() || time.isEmpty()) {
                        Toast.makeText(context, "Please fill in all primary details to book table", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.bookTable(
                            customerName = name,
                            customerEmail = email,
                            customerPhone = phone,
                            date = date,
                            time = time,
                            guests = guests.toIntOrNull() ?: 2,
                            specialRequest = request,
                            onSuccess = {
                                isBookedSuccessfully = true
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_table_booking_button")
            ) {
                Text("Confirm Advance Booking", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- SCREEN 6: CUSTOMER FEEDBACKS ---
@Composable
fun FeedbackScreen(viewModel: RestaurantViewModel) {
    val feedbacks by viewModel.feedbacks.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5) } // Default 5 star

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Customer Feedback Portal", style = MaterialTheme.typography.titleLarge, color = GoldPrimary)
        Text("Your ratings inspire our chefs to excel!", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(12.dp))

        // Feedback entry card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Submit Your Royal Experience Review", color = GoldAccent, fontWeight = FontWeight.Bold)

                // Stars rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rating: ", color = TextPrimary)
                    for (i in 1..5) {
                        val isFilled = i <= rating
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$i Stars",
                                tint = GoldAccent
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_name_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Your Email", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedback_email_input")
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment / Special compliments", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = GoldDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("feedback_comment_input")
                )

                Button(
                    onClick = {
                        if (name.isEmpty() || email.isEmpty() || comment.isEmpty()) {
                            Toast.makeText(context, "Please fill in all feedback fields", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.submitFeedback(
                                customerName = name,
                                customerEmail = email,
                                rating = rating,
                                comment = comment,
                                onSuccess = {
                                    name = ""
                                    email = ""
                                    comment = ""
                                    rating = 5
                                    Toast.makeText(context, "Feedback registered! Thank you.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("submit_feedback_button")
                ) {
                    Text("Submit Complimentary Review", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History list feedbacks
        Text("Complimentary Reviews from Patrons", color = GoldPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        if (feedbacks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Be the first to leave a complimentary review!", color = TextSecondary, fontSize = 12.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(feedbacks) { review ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(review.customerName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Row {
                                    repeat(review.rating) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(review.comment, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: RESTRICTED ADMIN DASHBOARD ---
@Composable
fun AdminScreen(viewModel: RestaurantViewModel) {
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val reservations by viewModel.reservations.collectAsStateWithLifecycle()
    val feedbacks by viewModel.feedbacks.collectAsStateWithLifecycle()
    val menuItems by viewModel.menuItems.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var adminTabSelection by remember { mutableStateOf("Orders") } // "Orders", "Reservations", "Menu", "Compliments"

    val context = LocalContext.current

    if (!isAdmin) {
        // Admin Auth Login Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Area",
                        tint = GoldAccent,
                        modifier = Modifier.size(70.dp)
                    )
                    
                    Text(
                        text = "The Empire Control Tower",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Restricted access for subhu07web@gmail.com personnel only.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Personnel Email ID", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Encrypted Password Code", color = TextSecondary) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input")
                    )

                    Button(
                        onClick = {
                            val success = viewModel.loginAdmin(email.trim(), password.trim())
                            if (success) {
                                Toast.makeText(context, "Personnel Identity Verified!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Authentication failed! Incorrect code.", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_submit")
                    ) {
                        Text("Verify Authentication", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    // Authenticated Admin Dashboard Control Panel
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("The Empire Control Panel", style = MaterialTheme.typography.titleLarge, color = GoldPrimary)
                Text("Manager Active Session: subhu07web@gmail.com", color = TextSecondary, fontSize = 11.sp)
            }
            TextButton(onClick = { viewModel.logout() }) {
                Text("Close Console", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Tabs Selection Rows
        val adminTabs = listOf("Orders", "Reservations", "Menu", "Compliments")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(adminTabs) { tab ->
                val isSel = adminTabSelection == tab
                Button(
                    onClick = { adminTabSelection = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) GoldPrimary else DarkSurfaceElevated
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSel) Color.Black else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sub-panels based on selection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (adminTabSelection) {
                "Orders" -> AdminOrdersTab(orders = orders, onStatusUpdate = { id, stat -> viewModel.updateOrderStatus(id, stat) }, onDelete = { id -> viewModel.deleteOrder(id) })
                "Reservations" -> AdminReservationsTab(reservations = reservations, onStatusUpdate = { id, stat -> viewModel.updateReservationStatus(id, stat) })
                "Menu" -> AdminMenuTab(
                    menuItems = menuItems,
                    onAddItem = { n, d, p, c, img, pop -> viewModel.addNewMenuItem(n, d, p, c, img, pop) },
                    onDeleteItem = { id -> viewModel.deleteMenuItem(id) }
                )
                "Compliments" -> AdminComplimentsTab(feedbacks = feedbacks)
            }
        }
    }
}

@Composable
fun AdminOrdersTab(
    orders: List<Order>,
    onStatusUpdate: (Int, String) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer orders placed yet.", color = TextSecondary)
        }
        return
    }

    // Adaptive grid columns based on screen size
    val configuration = LocalConfiguration.current
    val gridCol = if (configuration.screenWidthDp >= 600) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridCol),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(orders) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, GoldDark, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = GoldPrimary)
                        Text(
                            text = order.status,
                            color = when (order.status) {
                                "Pending" -> OrderStatusYellow
                                "Preparing" -> OrderStatusYellow
                                "Out for Delivery" -> OrderStatusBlue
                                "Delivered" -> OrderStatusGreen
                                else -> TextPrimary
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Patron: ${order.customerName} (${order.customerPhone})", color = TextPrimary, fontSize = 13.sp)
                    Text("Destination: ${order.deliveryAddress}", color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Total: ₹${order.totalAmount.toInt()} | Pay: ${order.paymentMethod}", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Divider(color = GoldDark.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    // Status Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (order.status) {
                            "Pending" -> {
                                Button(
                                    onClick = { onStatusUpdate(order.id, "Preparing") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrderStatusYellow),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Accept & Cook", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "Preparing" -> {
                                Button(
                                    onClick = { onStatusUpdate(order.id, "Out for Delivery") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrderStatusBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Dispatch", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "Out for Delivery" -> {
                                Button(
                                    onClick = { onStatusUpdate(order.id, "Delivered") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OrderStatusGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Complete", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "Delivered" -> {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .background(OrderStatusGreen.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Meal Served ✓", color = OrderStatusGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDelete(order.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReservationsTab(reservations: List<Reservation>, onStatusUpdate: (Int, String) -> Unit) {
    if (reservations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active table bookings registered.", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(reservations) { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Patron: ${res.customerName} (${res.customerPhone})", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Schedule: ${res.date} at ${res.time} | Guests: ${res.guests}", color = GoldAccent, fontSize = 12.sp)
                        if (res.specialRequest.isNotEmpty()) {
                            Text("Compliment: '${res.specialRequest}'", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = res.status,
                            color = if (res.status == "Confirmed") OrderStatusGreen else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        if (res.status == "Confirmed") {
                            IconButton(onClick = { onStatusUpdate(res.id, "Completed") }) {
                                Icon(Icons.Default.Done, contentDescription = "Mark Complete", tint = OrderStatusGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMenuTab(
    menuItems: List<MenuItem>,
    onAddItem: (String, String, Double, String, String, Boolean) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mains") }
    var imageUrl by remember { mutableStateOf("") }
    var isPopular by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Form to Add Item
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add Royal Menu Item", color = GoldPrimary, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = GoldDark)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (₹)", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = GoldDark)
                    )

                    // Category text dropdown representation
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Cat (Mains/Starters/Desserts)", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = GoldDark)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Appetizing description details", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = GoldDark)
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Food Photo Web Link (Optional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, unfocusedBorderColor = GoldDark)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPopular,
                        onCheckedChange = { isPopular = it },
                        colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                    )
                    Text("Set as Most Popular dish", color = TextPrimary)
                }

                Button(
                    onClick = {
                        if (name.isEmpty() || price.isEmpty() || description.isEmpty()) {
                            Toast.makeText(context, "Please enter Title, Price, Description details", Toast.LENGTH_SHORT).show()
                        } else {
                            onAddItem(name, description, price.toDoubleOrNull() ?: 100.0, category, imageUrl, isPopular)
                            name = ""
                            description = ""
                            price = ""
                            imageUrl = ""
                            isPopular = false
                            Toast.makeText(context, "Dish listed in Royal Menu!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publish to SQLite Room DB Menu", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List and delete existing
        Text("Active Dishes List", color = GoldPrimary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(menuItems) { dish ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dish.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Category: ${dish.category} | Price: ₹${dish.price}", color = GoldAccent, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onDeleteItem(dish.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove dish", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminComplimentsTab(feedbacks: List<Feedback>) {
    if (feedbacks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("NoCompliments reviews submitted by patrons yet.", color = TextSecondary)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(feedbacks) { compli ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Compliment from: ${compli.customerName}", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row {
                            repeat(compli.rating) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text("Email: ${compli.customerEmail}", color = GoldAccent, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(compli.comment, color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

// --- UTILITY COMPONENT: NOTIFICATION HISTORY DIALOG ---
@Composable
fun NotificationHubDialog(notifications: List<NotificationAlert>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Simulated Notification Hub", color = GoldAccent, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GoldAccent)
                    }
                }

                Divider(color = GoldDark, modifier = Modifier.padding(vertical = 8.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No real-time status alerts triggers recorded yet.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications) { alert ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = when (alert.type) {
                                                        "SMS" -> OrderStatusYellow.copy(alpha = 0.2f)
                                                        "PUSH" -> OrderStatusBlue.copy(alpha = 0.2f)
                                                        "EMAIL" -> OrderStatusGreen.copy(alpha = 0.2f)
                                                        else -> GoldAccent.copy(alpha = 0.2f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = alert.type,
                                                fontSize = 9.sp,
                                                color = when (alert.type) {
                                                    "SMS" -> OrderStatusYellow
                                                    "PUSH" -> OrderStatusBlue
                                                    "EMAIL" -> OrderStatusGreen
                                                    else -> GoldAccent
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "Instant Alert",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = alert.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = TextAccent
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = alert.message,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
