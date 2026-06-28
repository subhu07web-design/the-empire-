// The Empire Restaurant - Core Application State & Business Logic

// 1. Initial Database Seeding
const SEEDED_MENU_ITEMS = [
    {
        id: 1,
        name: "Paneer Tikka Angare",
        description: "Succulent cottage cheese cubes marinated in aromatic spices and grilled in a clay oven with charcoal.",
        price: 280.0,
        category: "Starters",
        imageUrl: "https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?auto=format&fit=crop&q=80&w=600",
        isPopular: true
    },
    {
        id: 2,
        name: "Empire Crispy Spring Rolls",
        description: "Golden crispy rolls stuffed with exotic shredded vegetables and glass noodles, served with spicy dip.",
        price: 190.0,
        category: "Starters",
        imageUrl: "https://images.unsplash.com/photo-1544025162-d76694265947?auto=format&fit=crop&q=80&w=600",
        isPopular: false
    },
    {
        id: 3,
        name: "Tandoori Chicken Kebab Platter",
        description: "An assortment of sizzling chicken seekh, tikka, and malai kebabs served with fresh mint chutney.",
        price: 390.0,
        category: "Starters",
        imageUrl: "https://images.unsplash.com/photo-1626132647523-66f5bf380027?auto=format&fit=crop&q=80&w=600",
        isPopular: true
    },
    {
        id: 4,
        name: "Royal Empire Butter Chicken",
        description: "Slices of tandoori grilled chicken slowly cooked in a rich, velvety tomato gravy loaded with butter and cream.",
        price: 450.0,
        category: "Mains",
        imageUrl: "https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?auto=format&fit=crop&q=80&w=600",
        isPopular: true
    },
    {
        id: 5,
        name: "Deccani Chicken Biryani",
        description: "Fragrant long-grain basmati rice layered with cooked spiced tender chicken, saffron, and fresh mint in traditional Dum style.",
        price: 420.0,
        category: "Mains",
        imageUrl: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&q=80&w=600",
        isPopular: true
    },
    {
        id: 6,
        name: "Paneer Lababdar",
        description: "Fresh cottage cheese in a rich, chunky onion-tomato cream sauce, served with traditional spices.",
        price: 340.0,
        category: "Mains",
        imageUrl: "https://images.unsplash.com/photo-1601050690597-df056fb4ce78?auto=format&fit=crop&q=80&w=600",
        isPopular: false
    },
    {
        id: 7,
        name: "Wild Mushroom & Truffle Risotto",
        description: "Premium Italian arborio rice cooked to creamy perfection with wild forest mushrooms, parmesan cheese, and black truffle oil.",
        price: 380.0,
        category: "Mains",
        imageUrl: "https://images.unsplash.com/photo-1476124369491-e7addf5db371?auto=format&fit=crop&q=80&w=600",
        isPopular: false
    },
    {
        id: 8,
        name: "Sizzling Chocolate Fudge Brownie",
        description: "Warm chocolate fudge brownie loaded with walnuts, served sizzling on a iron plate with premium vanilla ice cream.",
        price: 240.0,
        category: "Desserts",
        imageUrl: "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&q=80&w=600",
        isPopular: true
    },
    {
        id: 9,
        name: "Gulab Jamun with Kesar Rabri",
        description: "Saffron infused golden-fried milk-solid dumplings dipped in warm cardamom sugar syrup, topped with thick condensed milk.",
        price: 180.0,
        category: "Desserts",
        imageUrl: "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&q=80&w=600",
        isPopular: false
    },
    {
        id: 10,
        name: "Fresh Mint & Basil Mojito",
        description: "A bubbly blend of muddled fresh mint, fragrant sweet basil, lime juice, brown sugar, topped with chilled soda.",
        price: 160.0,
        category: "Beverages",
        imageUrl: "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&q=80&w=600",
        isPopular: false
    }
];

const SEEDED_FEEDBACKS = [
    {
        name: "Aarav Sharma",
        rating: 5,
        message: "The Butter Chicken at The Empire is simply legendary! Smooth, rich, and cooked to charcoal perfection. Truly a royal feast.",
        date: "2026-06-25"
    },
    {
        name: "Priyanka Patel",
        rating: 4,
        message: "Booked a candlelit table for our anniversary. The scenic balcony seating was spectacular, and the service was pristine. Will visit again!",
        date: "2026-06-26"
    }
];

// Initialize State from LocalStorage
let state = {
    menuItems: JSON.parse(localStorage.getItem('empire_menu')) || SEEDED_MENU_ITEMS,
    cart: JSON.parse(localStorage.getItem('empire_cart')) || [],
    orders: JSON.parse(localStorage.getItem('empire_orders')) || [],
    reservations: JSON.parse(localStorage.getItem('empire_reservations')) || [],
    feedbacks: JSON.parse(localStorage.getItem('empire_feedback')) || SEEDED_FEEDBACKS,
    currentTrackingOrderId: JSON.parse(localStorage.getItem('empire_tracking_id')) || null,
    notifications: JSON.parse(localStorage.getItem('empire_notifications')) || [],
    currentCategory: "All",
    adminUnlocked: sessionStorage.getItem('empire_admin_active') === "true" || false
};

// Save helper
function saveState() {
    localStorage.setItem('empire_menu', JSON.stringify(state.menuItems));
    localStorage.setItem('empire_cart', JSON.stringify(state.cart));
    localStorage.setItem('empire_orders', JSON.stringify(state.orders));
    localStorage.setItem('empire_reservations', JSON.stringify(state.reservations));
    localStorage.setItem('empire_feedback', JSON.stringify(state.feedbacks));
    localStorage.setItem('empire_tracking_id', JSON.stringify(state.currentTrackingOrderId));
    localStorage.setItem('empire_notifications', JSON.stringify(state.notifications));
}

// 2. Navigation System
function navigateTo(screenId) {
    // Hide all screens
    document.querySelectorAll('.screen-view').forEach(screen => {
        screen.classList.add('hidden');
    });

    // Show selected screen
    const targetScreen = document.getElementById(`screen-${screenId}`);
    if (targetScreen) {
        targetScreen.classList.remove('hidden');
    }

    // Update active state in Desktop Header
    document.querySelectorAll('.nav-btn').forEach(btn => {
        if (btn.id === `nav-${screenId}`) {
            btn.classList.add('active-nav');
        } else {
            btn.classList.remove('active-nav');
        }
    });

    // Update active state in Mobile Bottom Bar
    document.querySelectorAll('.mob-nav-btn').forEach(btn => {
        if (btn.id === `mob-nav-${screenId}`) {
            btn.classList.add('active-mob-nav');
        } else {
            btn.classList.remove('active-mob-nav');
        }
    });

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Initialize/Render context when screen is entered
    if (screenId === 'menu') {
        renderMenuItems();
    } else if (screenId === 'cart') {
        renderCart();
    } else if (screenId === 'tracker') {
        renderTracker();
    } else if (screenId === 'feedback') {
        renderFeedback();
    } else if (screenId === 'admin') {
        renderAdmin();
    }
}

// 3. Menu Screen Logic
function selectCategory(category) {
    state.currentCategory = category;
    document.querySelectorAll('.category-tab').forEach(tab => {
        if (tab.id === `cat-${category}`) {
            tab.classList.add('active-cat-tab');
        } else {
            tab.classList.remove('active-cat-tab');
        }
    });
    renderMenuItems();
}

function filterMenuItems() {
    renderMenuItems();
}

function renderMenuItems() {
    const container = document.getElementById('menu-grid');
    const searchVal = document.getElementById('menu-search-input').value.toLowerCase();
    const popularOnly = document.getElementById('menu-popular-only').checked;

    let itemsToRender = state.menuItems;

    // Filter Category
    if (state.currentCategory !== "All") {
        itemsToRender = itemsToRender.filter(item => item.category === state.currentCategory);
    }

    // Filter Search
    if (searchVal) {
        itemsToRender = itemsToRender.filter(item => 
            item.name.toLowerCase().includes(searchVal) || 
            item.description.toLowerCase().includes(searchVal)
        );
    }

    // Filter Popular
    if (popularOnly) {
        itemsToRender = itemsToRender.filter(item => item.isPopular);
    }

    if (itemsToRender.length === 0) {
        container.innerHTML = `
            <div class="col-span-full text-center py-12 text-[#B0B0B0]">
                <i class="fa-solid fa-face-frown text-4xl mb-3 text-[#D4AF37]/40 block"></i>
                No dishes match your current filter. Please try a different search!
            </div>
        `;
        return;
    }

    container.innerHTML = itemsToRender.map(item => {
        const cartItem = state.cart.find(ci => ci.menuItemId === item.id);
        const inCartQuantity = cartItem ? cartItem.quantity : 0;

        return `
            <div class="bg-[#1F1F1F] rounded-xl overflow-hidden border border-[#D4AF37]/15 hover:border-[#D4AF37]/40 transition-all duration-300 flex flex-col group hover:shadow-[0_4px_20px_rgba(212,175,55,0.1)]">
                <!-- Image with popular ribbon -->
                <div class="relative h-48 overflow-hidden bg-zinc-900">
                    <img src="${item.imageUrl}" alt="${item.name}" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500">
                    ${item.isPopular ? `
                        <div class="absolute top-3 left-3 bg-gradient-to-r from-[#D4AF37] to-[#996515] text-black text-[10px] font-extrabold uppercase px-2 py-1 rounded shadow-md tracking-wider">
                            <i class="fa-solid fa-star mr-1"></i> Royal Signature
                        </div>
                    ` : ''}
                    <div class="absolute top-3 right-3 bg-black/70 backdrop-blur-sm text-[#FFC107] text-[11px] font-bold px-2 py-1 rounded">
                        ${item.category}
                    </div>
                </div>
                <!-- Details -->
                <div class="p-5 flex-grow flex flex-col justify-between">
                    <div>
                        <h3 class="text-lg font-bold text-[#F3E5AB] group-hover:text-[#FFC107] transition-colors">${item.name}</h3>
                        <p class="text-xs text-[#B0B0B0] mt-1.5 line-clamp-2 leading-relaxed">${item.description}</p>
                    </div>
                    <!-- Actions -->
                    <div class="mt-5 flex items-center justify-between">
                        <span class="text-lg font-bold text-[#FFC107]">₹${item.price.toFixed(2)}</span>
                        
                        ${inCartQuantity > 0 ? `
                            <div class="flex items-center space-x-2 bg-[#282828] border border-[#D4AF37]/30 rounded-lg p-1">
                                <button onclick="updateCartQuantity(${item.id}, -1)" class="w-7 h-7 flex items-center justify-center rounded-md bg-[#121212] hover:bg-[#D4AF37]/20 text-[#D4AF37] transition-colors"><i class="fa-solid fa-minus text-xs"></i></button>
                                <span class="text-sm font-bold w-6 text-center text-white">${inCartQuantity}</span>
                                <button onclick="updateCartQuantity(${item.id}, 1)" class="w-7 h-7 flex items-center justify-center rounded-md bg-[#121212] hover:bg-[#D4AF37]/20 text-[#D4AF37] transition-colors"><i class="fa-solid fa-plus text-xs"></i></button>
                            </div>
                        ` : `
                            <button onclick="addToCart(${item.id})" class="px-3.5 py-1.5 bg-[#D4AF37] hover:bg-[#FFC107] text-black text-xs font-bold rounded-lg transition-all transform active:scale-95 flex items-center space-x-1">
                                <i class="fa-solid fa-cart-plus"></i> <span>Add to Cart</span>
                            </button>
                        `}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Render popular choices in Home Screen
function renderPopularShowcase() {
    const showcaseGrid = document.getElementById('popular-showcase-grid');
    if (!showcaseGrid) return;

    const popularItems = state.menuItems.filter(item => item.isPopular).slice(0, 3);

    showcaseGrid.innerHTML = popularItems.map(item => `
        <div class="bg-[#1F1F1F] rounded-xl overflow-hidden border border-[#D4AF37]/15 hover:border-[#D4AF37]/40 transition-all duration-300 flex flex-col group">
            <div class="relative h-44 overflow-hidden bg-zinc-900">
                <img src="${item.imageUrl}" alt="${item.name}" class="w-full h-full object-cover group-hover:scale-105 transition-all duration-500">
                <div class="absolute top-3 left-3 bg-gradient-to-r from-[#D4AF37] to-[#996515] text-black text-[9px] font-bold uppercase px-2 py-0.5 rounded shadow">
                    Signature Choice
                </div>
            </div>
            <div class="p-4 flex-grow flex flex-col justify-between">
                <div>
                    <h3 class="font-bold text-[#F3E5AB] group-hover:text-[#FFC107] text-base transition-colors">${item.name}</h3>
                    <p class="text-xs text-[#B0B0B0] mt-1.5 line-clamp-2 leading-relaxed">${item.description}</p>
                </div>
                <div class="mt-4 flex items-center justify-between">
                    <span class="text-base font-bold text-[#FFC107]">₹${item.price.toFixed(2)}</span>
                    <button onclick="addToCartAndGo(${item.id})" class="px-3 py-1 bg-gradient-to-r from-[#D4AF37] to-[#996515] hover:from-[#FFC107] hover:to-[#D4AF37] text-black text-[11px] font-bold rounded flex items-center space-x-1">
                        <span>Order Now</span> <i class="fa-solid fa-arrow-right text-[10px]"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

function addToCartAndGo(itemId) {
    addToCart(itemId);
    navigateTo('cart');
}

// 4. Cart & Checkout Operations
function addToCart(itemId) {
    const item = state.menuItems.find(m => m.id === itemId);
    if (!item) return;

    const existingCartIndex = state.cart.findIndex(c => c.menuItemId === itemId);
    if (existingCartIndex >= 0) {
        state.cart[existingCartIndex].quantity += 1;
    } else {
        state.cart.push({
            menuItemId: item.id,
            name: item.name,
            price: item.price,
            quantity: 1,
            imageUrl: item.imageUrl
        });
    }

    saveState();
    updateBadges();
    showToast("Added to Cart", `${item.name} added to your Royal Cart.`, "PUSH");
    
    // Refresh menus if on menu tab
    if (!document.getElementById('screen-menu').classList.contains('hidden')) {
        renderMenuItems();
    }
}

function updateCartQuantity(itemId, change) {
    const index = state.cart.findIndex(c => c.menuItemId === itemId);
    if (index >= 0) {
        state.cart[index].quantity += change;
        if (state.cart[index].quantity <= 0) {
            state.cart.splice(index, 1);
            showToast("Removed from Cart", "An item was removed from your cart.", "PUSH");
        }
        saveState();
        updateBadges();
        
        // Refresh appropriate view
        if (!document.getElementById('screen-menu').classList.contains('hidden')) {
            renderMenuItems();
        }
        if (!document.getElementById('screen-cart').classList.contains('hidden')) {
            renderCart();
        }
    }
}

function renderCart() {
    const container = document.getElementById('cart-items-container');
    const summaryPanel = document.getElementById('cart-summary-panel');

    if (state.cart.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12 text-[#B0B0B0]">
                <i class="fa-solid fa-cart-shopping text-4xl mb-3 text-[#D4AF37]/40 block animate-pulse"></i>
                Your cart is empty. Explore the <a href="#" onclick="navigateTo('menu'); return false;" class="text-[#FFC107] hover:underline">Royal Menu</a> to add items!
            </div>
        `;
        summaryPanel.classList.add('hidden');
        return;
    }

    summaryPanel.classList.remove('hidden');

    let total = 0;
    container.innerHTML = state.cart.map(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        return `
            <div class="flex items-center justify-between border-b border-[#282828] pb-4 gap-4">
                <div class="flex items-center space-x-3.5">
                    <img src="${item.imageUrl}" class="w-14 h-14 object-cover rounded-lg border border-[#D4AF37]/10" alt="${item.name}">
                    <div>
                        <h4 class="font-bold text-sm text-[#F3E5AB]">${item.name}</h4>
                        <span class="text-xs text-[#FFC107] font-semibold">₹${item.price.toFixed(2)}</span>
                    </div>
                </div>
                <div class="flex items-center space-x-3">
                    <div class="flex items-center space-x-1.5 bg-[#282828] rounded p-0.5 border border-[#D4AF37]/10">
                        <button onclick="updateCartQuantity(${item.menuItemId}, -1)" class="w-6 h-6 flex items-center justify-center rounded text-[#D4AF37] hover:bg-[#121212]"><i class="fa-solid fa-minus text-[10px]"></i></button>
                        <span class="text-xs font-bold w-5 text-center text-white">${item.quantity}</span>
                        <button onclick="updateCartQuantity(${item.menuItemId}, 1)" class="w-6 h-6 flex items-center justify-center rounded text-[#D4AF37] hover:bg-[#121212]"><i class="fa-solid fa-plus text-[10px]"></i></button>
                    </div>
                    <span class="text-sm font-extrabold text-white w-16 text-right">₹${itemTotal.toFixed(2)}</span>
                </div>
            </div>
        `;
    }).join('');

    // Update prices in checkout summary
    document.getElementById('summary-subtotal').innerText = `₹${total.toFixed(2)}`;
    document.getElementById('summary-total').innerText = `₹${total.toFixed(2)}`;
}

function handleCheckout(event) {
    event.preventDefault();

    const name = document.getElementById('checkout-name').value;
    const email = document.getElementById('checkout-email').value;
    const address = document.getElementById('checkout-address').value || "Dine-in Order";
    const mode = document.getElementById('checkout-type').value;
    const payment = document.getElementById('checkout-payment').value;

    const total = state.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    const newOrderId = Math.floor(10000 + Math.random() * 90000);
    const newOrder = {
        id: newOrderId,
        customerName: name,
        customerEmail: email,
        address: address,
        orderType: mode,
        paymentMethod: payment,
        totalAmount: total,
        status: "Pending",
        timestamp: Date.now(),
        items: [...state.cart]
    };

    // Save to state
    state.orders.push(newOrder);
    state.currentTrackingOrderId = newOrderId;
    state.cart = []; // Empty cart
    saveState();
    updateBadges();

    // Trigger Notification Event
    addAlertNotification({
        type: "SMS",
        title: "Order Placed Successfully",
        message: `[SMS] Dear ${name}, your order #${newOrderId} for ₹${total.toFixed(2)} at The Empire has been placed. Tracking: ${mode} mode.`
    });

    showToast("Royal Order Placed", `Order #${newOrderId} was created. Moving to Live Tracking...`, "PUSH");

    // Navigate to tracker
    setTimeout(() => {
        navigateTo('tracker');
    }, 1000);
}

// 5. Reservation System
function handleBooking(event) {
    event.preventDefault();

    const name = document.getElementById('book-name').value;
    const phone = document.getElementById('book-phone').value;
    const date = document.getElementById('book-date').value;
    const time = document.getElementById('book-time').value;
    const guests = document.getElementById('book-guests').value;
    const seating = document.getElementById('book-seating').value;
    const notes = document.getElementById('book-notes').value || "No special decor request";

    const reservationId = Math.floor(1000 + Math.random() * 9000);
    const newReservation = {
        id: reservationId,
        customerName: name,
        phone: phone,
        date: date,
        timeSlot: time,
        guests: guests,
        seatingPreference: seating,
        notes: notes,
        status: "Approved" // Auto approved initially
    };

    state.reservations.push(newReservation);
    saveState();

    showToast("Reservation Confirmed", `Dear ${name}, Table for ${guests} is reserved on ${date} at ${time}. Code: EM-${reservationId}`, "PUSH");
    
    // Add VVIP notification
    addAlertNotification({
        type: "EMAIL",
        title: "Table Reserved at The Empire",
        message: `[Email] To: patron@empire.com\nSubject: Confirmation: Table Reservation Code EM-${reservationId}\n\nHi ${name},\nYour table reservation for ${guests} at our elegant ${seating} has been locked on ${date} for ${time}. See you there!`
    });

    // Reset Form
    document.getElementById('booking-form').reset();
}

// 6. Tracker Screen Logic
function renderTracker() {
    const emptyPanel = document.getElementById('tracker-empty');
    const activePanel = document.getElementById('tracker-active');

    if (!state.currentTrackingOrderId) {
        emptyPanel.classList.remove('hidden');
        activePanel.classList.add('hidden');
        return;
    }

    emptyPanel.classList.add('hidden');
    activePanel.classList.remove('hidden');

    const activeOrder = state.orders.find(o => o.id === state.currentTrackingOrderId);
    if (!activeOrder) {
        state.currentTrackingOrderId = null;
        saveState();
        renderTracker();
        return;
    }

    // Set Header Summary details
    document.getElementById('track-order-id').innerText = `#${activeOrder.id}`;
    document.getElementById('track-status-badge').innerText = activeOrder.status;

    // Timeline steps activation
    const steps = ["Pending", "Preparing", "Out for Delivery", "Delivered"];
    const activeIndex = steps.indexOf(activeOrder.status);

    steps.forEach((step, idx) => {
        const stepEl = document.getElementById(`step-${getStepId(step)}`);
        if (!stepEl) return;

        stepEl.className = "flex items-start space-x-4 relative transition-all duration-300 ";
        
        if (idx < activeIndex) {
            stepEl.classList.add("step-completed");
        } else if (idx === activeIndex) {
            stepEl.classList.add("step-active");
        } else {
            stepEl.classList.add("step-inactive");
        }
    });

    // Render receipt summary inside tracker page
    const trackerSummaryContainer = document.getElementById('tracker-summary-items');
    trackerSummaryContainer.innerHTML = activeOrder.items.map(item => `
        <div class="flex justify-between text-xs py-1">
            <span class="text-[#B0B0B0]">${item.name} <span class="font-bold text-white">x${item.quantity}</span></span>
            <span class="text-white">₹${(item.price * item.quantity).toFixed(2)}</span>
        </div>
    `).join('');

    document.getElementById('tracker-summary-total').innerText = `₹${activeOrder.totalAmount.toFixed(2)}`;
    document.getElementById('tracker-summary-mode').innerText = activeOrder.orderType;
    document.getElementById('tracker-summary-payment').innerText = activeOrder.paymentMethod;
    document.getElementById('tracker-summary-address').innerText = activeOrder.address;
}

function getStepId(status) {
    if (status === "Pending") return "pending";
    if (status === "Preparing") return "preparing";
    if (status === "Out for Delivery") return "shipping";
    if (status === "Delivered") return "delivered";
    return "pending";
}

// Live Status Stepper progression simulator
function simulateNextStatus() {
    if (!state.currentTrackingOrderId) return;

    const orderIndex = state.orders.findIndex(o => o.id === state.currentTrackingOrderId);
    if (orderIndex < 0) return;

    const order = state.orders[orderIndex];
    let nextStatus = "Pending";

    if (order.status === "Pending") {
        nextStatus = "Preparing";
        addAlertNotification({
            type: "PUSH",
            title: "Chef is Preparing Your Food",
            message: `[Push] The Empire: Order #${order.id} is now in the kitchen! Our master chefs are cooking with royal coal furnaces.`
        });
    } else if (order.status === "Preparing") {
        nextStatus = "Out for Delivery";
        addAlertNotification({
            type: "SMS",
            title: "Order Dispatched",
            message: `[SMS] The Empire: Your order #${order.id} is out for delivery! Executive has picked up and is on the way.`
        });
        addAlertNotification({
            type: "EMAIL",
            title: "Shipping Status Update",
            message: `[Email] To: ${order.customerEmail}\nSubject: Order #${order.id} is Out for Delivery!\n\nHi ${order.customerName},\nYour meal is freshly packed from The Empire and dispatched. It will reach your doorstep shortly!`
        });
    } else if (order.status === "Out for Delivery") {
        nextStatus = "Delivered";
        addAlertNotification({
            type: "PUSH",
            title: "Order Delivered",
            message: `[Push] The Empire: Order #${order.id} has been delivered successfully. Enjoy your royal meal and share feedback!`
        });
        addAlertNotification({
            type: "EMAIL",
            title: "Order Delivered Invoice",
            message: `[Email] To: ${order.customerEmail}\nSubject: Invoice for Order #${order.id}\n\nHi ${order.customerName},\nYour order #${order.id} is delivered. Total of ₹${order.totalAmount.toFixed(2)} was processed via ${order.paymentMethod}.`
        });
    } else {
        // Reset or keep delivered
        showToast("Order Already Delivered", `Order #${order.id} was completed. Placing a new order will let you simulate again.`, "PUSH");
        return;
    }

    state.orders[orderIndex].status = nextStatus;
    saveState();
    renderTracker();
    showToast("Status Updated", `Simulated Status: ${nextStatus}`, "PUSH");
}

// 7. Feedback Screen Logic
function setFeedbackRating(stars) {
    document.getElementById('feedback-rating').value = stars;
    for (let i = 1; i <= 5; i++) {
        const star = document.getElementById(`star-${i}`);
        if (i <= stars) {
            star.classList.add('star-selected', 'text-[#FFC107]');
            star.classList.remove('text-gray-600');
        } else {
            star.classList.remove('star-selected', 'text-[#FFC107]');
            star.classList.add('text-gray-600');
        }
    }
}

function handleFeedback(event) {
    event.preventDefault();

    const name = document.getElementById('feedback-name').value;
    const rating = parseInt(document.getElementById('feedback-rating').value);
    const message = document.getElementById('feedback-message').value;

    const newFeedback = {
        name: name,
        rating: rating,
        message: message,
        date: new Date().toISOString().split('T')[0]
    };

    state.feedbacks.unshift(newFeedback);
    saveState();
    renderFeedback();

    showToast("Feedback Submitted", "Thank you! We appreciate your thoughts and support.", "PUSH");

    // Reset feedback fields
    document.getElementById('feedback-form').reset();
    setFeedbackRating(5);
}

function renderFeedback() {
    const container = document.getElementById('feedback-showcase');
    if (!container) return;

    if (state.feedbacks.length === 0) {
        container.innerHTML = `<div class="text-[#B0B0B0] text-sm py-4">No reviews submitted yet. Be the first!</div>`;
        return;
    }

    container.innerHTML = state.feedbacks.map(f => {
        let starsHtml = "";
        for (let i = 1; i <= 5; i++) {
            starsHtml += `<i class="fa-solid fa-star text-sm ${i <= f.rating ? 'text-[#FFC107]' : 'text-gray-600'}"></i>`;
        }

        return `
            <div class="bg-[#1F1F1F] p-5 rounded-xl border border-[#D4AF37]/10 space-y-3 shadow-md">
                <div class="flex justify-between items-center">
                    <div>
                        <h4 class="font-bold text-sm text-[#F3E5AB]">${f.name}</h4>
                        <span class="text-[10px] text-[#B0B0B0]">${f.date}</span>
                    </div>
                    <div class="flex space-x-0.5">
                        ${starsHtml}
                    </div>
                </div>
                <p class="text-xs text-[#B0B0B0] leading-relaxed italic">"${f.message}"</p>
            </div>
        `;
    }).join('');
}

// 8. Alerts Hub / Notification Drawer Center
function addAlertNotification(alert) {
    state.notifications.unshift({
        type: alert.type,
        title: alert.title,
        message: alert.message,
        timestamp: Date.now()
    });
    saveState();
    updateBadges();
    renderNotificationList();
    
    // Slide beautiful overlay notifications toast
    showToast(alert.title, alert.message, alert.type);
}

function renderNotificationList() {
    const container = document.getElementById('notification-list');
    if (!container) return;

    if (state.notifications.length === 0) {
        container.innerHTML = `
            <div class="p-4 text-center text-[#B0B0B0] text-sm py-8">
                <i class="fa-regular fa-bell-slash text-2xl mb-2 text-[#D4AF37]/40 block"></i>
                No active alerts. Orders will update you here!
            </div>
        `;
        return;
    }

    container.innerHTML = state.notifications.map(notif => {
        let badgeColor = "bg-[#FFC107]/20 text-[#FFC107] border-[#FFC107]/30";
        let icon = "fa-bell";

        if (notif.type === "SMS") {
            badgeColor = "bg-sky-500/15 text-sky-400 border-sky-500/30";
            icon = "fa-comment-sms";
        } else if (notif.type === "EMAIL") {
            badgeColor = "bg-green-500/15 text-green-400 border-green-500/30";
            icon = "fa-envelope";
        }

        return `
            <div class="p-4 hover:bg-[#282828] transition-all flex items-start gap-3.5">
                <div class="w-8 h-8 rounded-lg flex items-center justify-center text-xs shrink-0 border ${badgeColor}">
                    <i class="fa-solid ${icon}"></i>
                </div>
                <div class="space-y-1">
                    <div class="flex items-center space-x-1.5">
                        <span class="text-xs font-bold text-white">${notif.title}</span>
                        <span class="text-[9px] uppercase border px-1 rounded font-semibold tracking-wider ${badgeColor}">${notif.type}</span>
                    </div>
                    <p class="text-xs text-[#B0B0B0] whitespace-pre-line leading-relaxed">${notif.message}</p>
                    <span class="text-[9px] text-zinc-500 block">${new Date(notif.timestamp).toLocaleTimeString()}</span>
                </div>
            </div>
        `;
    }).join('');
}

function clearNotifications() {
    state.notifications = [];
    saveState();
    updateBadges();
    renderNotificationList();
}

function toggleNotificationHub() {
    const hub = document.getElementById('notification-hub');
    hub.classList.toggle('hidden');
    renderNotificationList();
}

// 9. Admin Operations Console Console Panel
function unlockAdmin(event) {
    event.preventDefault();
    const password = document.getElementById('admin-password').value;
    if (password === "admin") {
        state.adminUnlocked = true;
        sessionStorage.setItem('empire_admin_active', "true");
        renderAdmin();
    } else {
        alert("Incorrect Admin Access Password!");
    }
}

function lockAdminConsole() {
    state.adminUnlocked = false;
    sessionStorage.setItem('empire_admin_active', "false");
    renderAdmin();
}

function switchAdminTab(subtab) {
    document.querySelectorAll('.admin-subtab').forEach(tab => {
        if (tab.id === `adtab-${subtab}`) {
            tab.classList.add('active-ad-subtab');
        } else {
            tab.classList.remove('active-ad-subtab');
        }
    });

    document.querySelectorAll('.admin-view-sub').forEach(view => {
        view.classList.add('hidden');
    });

    document.getElementById(`admin-view-${subtab}`).classList.remove('hidden');
    
    if (subtab === 'orders') {
        renderAdminOrders();
    } else if (subtab === 'reservations') {
        renderAdminReservations();
    } else if (subtab === 'menu') {
        renderAdminMenuItems();
    } else if (subtab === 'feedback') {
        renderAdminFeedback();
    }
}

function renderAdmin() {
    const gate = document.getElementById('admin-gate');
    const dashboard = document.getElementById('admin-dashboard');

    if (!state.adminUnlocked) {
        gate.classList.remove('hidden');
        dashboard.classList.add('hidden');
        return;
    }

    gate.classList.add('hidden');
    dashboard.classList.remove('hidden');

    // Load metrics stats
    const totalSales = state.orders.reduce((sum, o) => o.status === "Delivered" ? sum + o.totalAmount : sum, 0);
    document.getElementById('stat-sales').innerText = `₹${totalSales.toFixed(2)}`;
    document.getElementById('stat-orders').innerText = state.orders.length;
    document.getElementById('stat-tables').innerText = state.reservations.length;
    document.getElementById('stat-feedback').innerText = state.feedbacks.length;

    // Load active orders as sub-view on launch
    switchAdminTab('orders');
}

function renderAdminOrders() {
    const body = document.getElementById('admin-orders-table-body');
    if (state.orders.length === 0) {
        body.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-[#B0B0B0] text-xs">No customer orders placed yet.</td></tr>`;
        return;
    }

    body.innerHTML = state.orders.map(order => {
        const itemsSummary = order.items.map(i => `${i.name} (x${i.quantity})`).join(', ');
        return `
            <tr class="hover:bg-[#282828] transition-colors">
                <td class="px-6 py-4 font-bold text-[#FFC107]">#${order.id}</td>
                <td class="px-6 py-4">
                    <div class="font-semibold text-white">${order.customerName}</div>
                    <div class="text-[10px] text-[#B0B0B0]">${order.customerEmail}</div>
                </td>
                <td class="px-6 py-4 max-w-xs truncate text-[#B0B0B0] text-xs" title="${itemsSummary}">${itemsSummary}</td>
                <td class="px-6 py-4 font-semibold text-white">₹${order.totalAmount.toFixed(2)}</td>
                <td class="px-6 py-4">
                    <select onchange="updateAdminOrderStatus(${order.id}, this.value)" class="bg-[#121212] border border-[#D4AF37]/20 text-xs text-[#FFC107] font-semibold px-2.5 py-1.5 rounded focus:outline-none focus:border-[#FFC107]">
                        <option value="Pending" ${order.status === 'Pending' ? 'selected' : ''}>Pending</option>
                        <option value="Preparing" ${order.status === 'Preparing' ? 'selected' : ''}>Preparing</option>
                        <option value="Out for Delivery" ${order.status === 'Out for Delivery' ? 'selected' : ''}>Out for Delivery</option>
                        <option value="Delivered" ${order.status === 'Delivered' ? 'selected' : ''}>Delivered</option>
                    </select>
                </td>
                <td class="px-6 py-4 text-center">
                    <button onclick="deleteAdminOrder(${order.id})" class="text-zinc-500 hover:text-red-500 transition-colors p-1" title="Delete Order"><i class="fa-solid fa-trash"></i></button>
                </td>
            </tr>
        `;
    }).join('');
}

function updateAdminOrderStatus(orderId, nextStatus) {
    const orderIdx = state.orders.findIndex(o => o.id === orderId);
    if (orderIdx >= 0) {
        const order = state.orders[orderIdx];
        state.orders[orderIdx].status = nextStatus;
        saveState();

        // Fire notifications
        if (nextStatus === "Preparing") {
            addAlertNotification({
                type: "PUSH",
                title: "Chef is Preparing Your Food",
                message: `[Push] The Empire: Order #${order.id} is now in the kitchen! Our chefs are preparing your gourmet dishes.`
            });
        } else if (nextStatus === "Out for Delivery") {
            addAlertNotification({
                type: "SMS",
                title: "Order Dispatched",
                message: `[SMS] The Empire: Your order #${order.id} has been dispatched! Prepare to serve.`
            });
            addAlertNotification({
                type: "EMAIL",
                title: "Shipping Status Update",
                message: `[Email] To: ${order.customerEmail}\nSubject: Order #${order.id} is Out for Delivery!\n\nHi ${order.customerName},\nYour meal has left our main kitchen.`
            });
        } else if (nextStatus === "Delivered") {
            addAlertNotification({
                type: "PUSH",
                title: "Order Delivered",
                message: `[Push] The Empire: Order #${order.id} has been delivered successfully. Enjoy!`
            });
        }

        showToast("Status Synchronized", `Order #${orderId} set to ${nextStatus}`, "PUSH");
        renderAdmin();
        if (state.currentTrackingOrderId === orderId) {
            renderTracker();
        }
    }
}

function deleteAdminOrder(orderId) {
    if (confirm(`Delete Order #${orderId} permanently?`)) {
        state.orders = state.orders.filter(o => o.id !== orderId);
        if (state.currentTrackingOrderId === orderId) {
            state.currentTrackingOrderId = null;
        }
        saveState();
        showToast("Order Purged", `Order #${orderId} deleted.`, "PUSH");
        renderAdmin();
    }
}

function renderAdminReservations() {
    const body = document.getElementById('admin-reservations-table-body');
    if (state.reservations.length === 0) {
        body.innerHTML = `<tr><td colspan="8" class="px-6 py-12 text-center text-[#B0B0B0] text-xs">No active bookings.</td></tr>`;
        return;
    }

    body.innerHTML = state.reservations.map(res => `
        <tr class="hover:bg-[#282828] transition-colors">
            <td class="px-6 py-4 font-bold text-white">${res.customerName}</td>
            <td class="px-6 py-4 font-medium text-[#B0B0B0]">${res.phone}</td>
            <td class="px-6 py-4 text-xs text-white">${res.date}<br><span class="text-[#FFC107] font-semibold">${res.timeSlot}</span></td>
            <td class="px-6 py-4 text-white font-semibold">${res.guests}</td>
            <td class="px-6 py-4 text-[#B0B0B0] text-xs">${res.seatingPreference}</td>
            <td class="px-6 py-4 text-zinc-400 max-w-xs truncate text-xs" title="${res.notes}">${res.notes}</td>
            <td class="px-6 py-4">
                <span class="px-2 py-1 text-[10px] rounded font-bold uppercase ${res.status === 'Approved' ? 'bg-green-500/10 text-green-400' : 'bg-red-500/15 text-red-400'}">${res.status}</span>
            </td>
            <td class="px-6 py-4 text-center space-x-1.5 whitespace-nowrap">
                <button onclick="toggleReservationStatus(${res.id}, 'Approved')" class="px-2 py-1 bg-green-500 text-black text-[10px] font-bold rounded hover:bg-green-400"><i class="fa-solid fa-check"></i></button>
                <button onclick="toggleReservationStatus(${res.id}, 'Cancelled')" class="px-2 py-1 bg-red-600 text-white text-[10px] font-bold rounded hover:bg-red-500"><i class="fa-solid fa-xmark"></i></button>
            </td>
        </tr>
    `).join('');
}

function toggleReservationStatus(id, status) {
    const resIdx = state.reservations.findIndex(r => r.id === id);
    if (resIdx >= 0) {
        state.reservations[resIdx].status = status;
        saveState();
        showToast("Reservation Updated", `Booking EM-${id} set to ${status}`, "PUSH");
        renderAdminReservations();
    }
}

function renderAdminMenuItems() {
    const body = document.getElementById('admin-menu-table-body');
    body.innerHTML = state.menuItems.map(item => `
        <tr class="hover:bg-[#282828] transition-colors">
            <td class="px-6 py-4 flex items-center space-x-3.5">
                <img src="${item.imageUrl}" class="w-10 h-10 object-cover rounded border border-[#D4AF37]/10" alt="${item.name}">
                <span class="font-bold text-white text-sm">${item.name}</span>
            </td>
            <td class="px-6 py-4 max-w-xs truncate text-xs text-[#B0B0B0]" title="${item.description}">${item.description}</td>
            <td class="px-6 py-4 text-xs font-semibold text-zinc-400">${item.category}</td>
            <td class="px-6 py-4 font-bold text-[#FFC107]">₹${item.price.toFixed(2)}</td>
            <td class="px-6 py-4 text-center font-bold text-lg">${item.isPopular ? '<span class="text-[#FFC107]">★</span>' : '<span class="text-zinc-600">☆</span>'}</td>
            <td class="px-6 py-4 text-center space-x-2 whitespace-nowrap">
                <button onclick="editMenuItem(${item.id})" class="text-sky-400 hover:text-sky-300"><i class="fa-solid fa-pen"></i></button>
                <button onclick="deleteMenuItem(${item.id})" class="text-zinc-500 hover:text-red-500"><i class="fa-solid fa-trash"></i></button>
            </td>
        </tr>
    `).join('');
}

function deleteMenuItem(id) {
    if (confirm("Delete this dish from menu database permanently?")) {
        state.menuItems = state.menuItems.filter(m => m.id !== id);
        saveState();
        showToast("Dish Purged", "MenuItem deleted from database.", "PUSH");
        renderAdminMenuItems();
    }
}

// Dialog Add/Edit Menu items
function openAddItemDialog() {
    document.getElementById('menu-modal-title').innerText = "Add New Menu Item";
    document.getElementById('menu-item-id').value = "";
    document.getElementById('menu-item-form').reset();
    document.getElementById('menu-modal').classList.remove('hidden');
}

function editMenuItem(id) {
    const item = state.menuItems.find(m => m.id === id);
    if (!item) return;

    document.getElementById('menu-modal-title').innerText = "Modify Menu Item Details";
    document.getElementById('menu-item-id').value = item.id;
    document.getElementById('menu-item-name').value = item.name;
    document.getElementById('menu-item-desc').value = item.description;
    document.getElementById('menu-item-category').value = item.category;
    document.getElementById('menu-item-price').value = item.price;
    document.getElementById('menu-item-image').value = item.imageUrl;
    document.getElementById('menu-item-popular').checked = item.isPopular;

    document.getElementById('menu-modal').classList.remove('hidden');
}

function closeMenuModal() {
    document.getElementById('menu-modal').classList.add('hidden');
}

function handleMenuItemSubmit(event) {
    event.preventDefault();

    const idVal = document.getElementById('menu-item-id').value;
    const name = document.getElementById('menu-item-name').value;
    const desc = document.getElementById('menu-item-desc').value;
    const category = document.getElementById('menu-item-category').value;
    const price = parseFloat(document.getElementById('menu-item-price').value);
    const img = document.getElementById('menu-item-image').value;
    const popular = document.getElementById('menu-item-popular').checked;

    if (idVal) {
        // Edit Mode
        const itemIdx = state.menuItems.findIndex(m => m.id === parseInt(idVal));
        if (itemIdx >= 0) {
            state.menuItems[itemIdx] = {
                id: parseInt(idVal),
                name,
                description: desc,
                category,
                price,
                imageUrl: img,
                isPopular: popular
            };
            showToast("MenuItem Saved", `${name} updated successfully!`, "PUSH");
        }
    } else {
        // Add Mode
        const newId = Math.max(...state.menuItems.map(m => m.id)) + 1;
        state.menuItems.push({
            id: newId,
            name,
            description: desc,
            category,
            price,
            imageUrl: img,
            isPopular: popular
        });
        showToast("MenuItem Created", `${name} appended to menu items.`, "PUSH");
    }

    saveState();
    closeMenuModal();
    renderAdminMenuItems();
}

function renderAdminFeedback() {
    const body = document.getElementById('admin-feedback-table-body');
    if (state.feedbacks.length === 0) {
        body.innerHTML = `<tr><td colspan="5" class="px-6 py-12 text-center text-[#B0B0B0] text-xs">No feedback submitted.</td></tr>`;
        return;
    }

    body.innerHTML = state.feedbacks.map((f, idx) => {
        let stars = "";
        for (let i = 1; i <= 5; i++) {
            stars += `<i class="fa-solid fa-star text-[10px] ${i <= f.rating ? 'text-[#FFC107]' : 'text-zinc-600'}"></i>`;
        }

        return `
            <tr class="hover:bg-[#282828] transition-colors">
                <td class="px-6 py-4 font-bold text-white">${f.name}</td>
                <td class="px-6 py-4 whitespace-nowrap">${stars}</td>
                <td class="px-6 py-4 text-xs text-[#B0B0B0] max-w-sm leading-relaxed">${f.message}</td>
                <td class="px-6 py-4 text-xs text-zinc-500">${f.date}</td>
                <td class="px-6 py-4 text-center">
                    <button onclick="deleteAdminFeedback(${idx})" class="text-zinc-500 hover:text-red-500" title="Delete Review"><i class="fa-solid fa-trash"></i></button>
                </td>
            </tr>
        `;
    }).join('');
}

function deleteAdminFeedback(idx) {
    if (confirm("Delete feedback review permanently?")) {
        state.feedbacks.splice(idx, 1);
        saveState();
        showToast("Review Deleted", "Guest testimonial removed.", "PUSH");
        renderAdminFeedback();
    }
}

// 10. Core UI Badges, Toast Overlay alerts
function updateBadges() {
    const totalQty = state.cart.reduce((sum, item) => sum + item.quantity, 0);

    // Desktop
    const deskBadge = document.getElementById('cart-badge-desktop');
    if (totalQty > 0) {
        deskBadge.innerText = totalQty;
        deskBadge.classList.remove('hidden');
    } else {
        deskBadge.classList.add('hidden');
    }

    // Mobile
    const mobBadge = document.getElementById('cart-badge-mobile');
    if (totalQty > 0) {
        mobBadge.innerText = totalQty;
        mobBadge.classList.remove('hidden');
    } else {
        mobBadge.classList.add('hidden');
    }

    // Tracker active order status animations/dots
    const deskDot = document.getElementById('tracker-dot-desktop');
    const mobDot = document.getElementById('tracker-dot-mobile');

    if (state.currentTrackingOrderId) {
        deskDot.classList.remove('hidden');
        mobDot.classList.remove('hidden');
    } else {
        deskDot.classList.add('hidden');
        mobDot.classList.add('hidden');
    }

    // Notifications Badge count
    const notifBadge = document.getElementById('notif-badge');
    if (state.notifications.length > 0) {
        notifBadge.innerText = state.notifications.length;
        notifBadge.classList.remove('hidden');
    } else {
        notifBadge.classList.add('hidden');
    }
}

// Custom Premium Toast Pop-ups
function showToast(title, message, type) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    let toastIcon = "fa-bell";
    let borderTheme = "border-[#D4AF37]/30 bg-[#1F1F1F]";
    let textTheme = "text-[#FFC107]";

    if (type === "SMS") {
        toastIcon = "fa-comment-sms";
        borderTheme = "border-sky-500/30 bg-[#121c2c]";
        textTheme = "text-sky-400";
    } else if (type === "EMAIL") {
        toastIcon = "fa-envelope";
        borderTheme = "border-green-500/30 bg-[#12281a]";
        textTheme = "text-green-400";
    }

    const toastId = "toast_" + Date.now() + Math.floor(Math.random() * 100);
    const toastHtml = `
        <div id="${toastId}" class="toast-slide-in p-4 rounded-xl border ${borderTheme} shadow-2xl flex items-start gap-3.5 max-w-sm w-full backdrop-blur-md">
            <div class="w-9 h-9 rounded-lg shrink-0 flex items-center justify-center text-sm bg-black/40 border border-white/5 ${textTheme}">
                <i class="fa-solid ${toastIcon}"></i>
            </div>
            <div class="flex-grow space-y-0.5">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold text-white">${title}</span>
                    <span class="text-[9px] uppercase border px-1 rounded font-bold tracking-widest bg-black/40 border-white/5 ${textTheme}">${type}</span>
                </div>
                <p class="text-[11px] text-[#B0B0B0] leading-relaxed">${message}</p>
            </div>
            <button onclick="dismissToast('${toastId}')" class="text-zinc-500 hover:text-white shrink-0"><i class="fa-solid fa-xmark text-sm"></i></button>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);

    // Auto-dismiss after 6.5s
    setTimeout(() => {
        dismissToast(toastId);
    }, 6500);
}

function dismissToast(id) {
    const el = document.getElementById(id);
    if (el) {
        el.classList.add('opacity-0', 'scale-90');
        el.classList.remove('toast-slide-in');
        setTimeout(() => {
            el.remove();
        }, 300);
    }
}

// 11. Initializers
window.addEventListener('DOMContentLoaded', () => {
    updateBadges();
    renderPopularShowcase();
    setFeedbackRating(5);
    
    // Default screen
    navigateTo('home');
});
