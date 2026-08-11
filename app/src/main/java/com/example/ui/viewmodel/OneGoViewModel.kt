package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.HistoryEntity
import com.example.data.database.OneGoDatabase
import com.example.data.database.SavedPlaceEntity
import com.example.data.repository.FirebaseSyncManager
import com.example.data.repository.GeminiSearchRepository
import com.example.data.repository.MapsGroundingResult
import com.example.data.repository.OneGoRepository
import com.example.data.repository.SearchGroundingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.*

// UI representation of compared options
data class RideOption(
    val platformId: String, // "grab", "maxim_moto", "maxim_car", "xicar", "gojek", "indrive", "angkas", "joyride"
    val platformName: String,
    val serviceName: String, // e.g. "GrabCar", "Moto Taxi", "Tricycle"
    val estimatedFare: Double,
    val etaMins: Int,
    val pickupEtaRange: String = "${max(1, etaMins - 1)} - ${etaMins + 2} mins",
    val destinationEtaRange: String = "${max(10, etaMins + 12)} - ${etaMins + 20} mins",
    val promoLabel: String? = null,
    val badgeLabel: String? = null, // e.g. "Featured: No Surge", "Fastest", "Cheapest"
    val deepLinkUrl: String
)

data class FoodOption(
    val platformId: String, // "grabfood", "foodpanda", "shopeefood", "gojekfood"
    val platformName: String,
    val basePrice: Double,
    val deliveryFee: Double,
    val serviceFee: Double,
    val estimatedTotal: Double,
    val etaMins: Int,
    val deliveryEtaRange: String = "${max(15, etaMins - 5)} - ${etaMins + 8} mins",
    val discountLabel: String? = null,
    val badgeLabel: String? = null,
    val deepLinkUrl: String
)

data class DeliveryOption(
    val platformId: String, // "grabexpress", "lalamove_moto", "lalamove_car", "maxim_delivery", "gojek_logistics"
    val platformName: String,
    val serviceName: String, // e.g. "Motorbike Instant", "Sedan MPV"
    val estimatedFare: Double,
    val etaMins: Int,
    val maxWeightKg: Double,
    val pickupEtaRange: String = "${max(2, etaMins - 5)} - ${max(5, etaMins - 2)} mins",
    val deliveryEtaRange: String = "${max(12, etaMins - 2)} - ${etaMins + 10} mins",
    val badgeLabel: String? = null,
    val deepLinkUrl: String
)

data class PabiliItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val estimatedPrice: Double,
    val quantity: Int = 1,
    val notes: String = "",
    val priceRange: String = "₱${(estimatedPrice * 0.85).toInt()} - ₱${(estimatedPrice * 1.20).toInt()}"
)

data class PabiliOption(
    val platformId: String, // "grab_pabili", "joyride_pabili", "maxim_pabili", "lalamove_pabili", "moveit_pabili"
    val platformName: String,
    val serviceName: String, // e.g. "GrabExpress Pabili", "Joyride Super Pabili"
    val deliveryFee: Double,
    val serviceFee: Double,
    val totalDeliveryAndServiceFee: Double,
    val etaMins: Int,
    val pickupEtaRange: String = "${max(3, etaMins - 8)} - ${max(7, etaMins - 4)} mins",
    val deliveryEtaRange: String = "${max(20, etaMins - 3)} - ${etaMins + 12} mins",
    val maxShoppingBudget: Double = 2000.0,
    val badgeLabel: String? = null,
    val promoLabel: String? = null,
    val deepLinkUrl: String
)

// Restaurant menu item
data class Restaurant(
    val id: String,
    val name: String,
    val cuisines: String,
    val rating: Double,
    val itemPrice: Double,
    val sampleItemName: String,
    val iconResId: String, // Name of icon descriptor
    val categories: List<String> = emptyList(),
    val imageUri: String? = null,
    val isUserStore: Boolean = false,
    val address: String = "Metro Iloilo City",
    val latitude: Double = 10.7123,
    val longitude: Double = 122.5518,
    val itemPriceRange: String = "₱${(itemPrice * 0.85).toInt()} - ₱${(itemPrice * 1.35).toInt()}"
)

data class PaymentMethod(
    val id: String,
    val type: String, // "CARD", "GCASH", "MAYA", "CASH"
    val label: String,
    val detail: String,
    val isDefault: Boolean = false,
    val cardholderName: String = "",
    val expiryDate: String = "",
    val billingPhone: String = ""
)

class OneGoViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = com.example.data.auth.FirebaseAuthManager(application)
    private val firebaseSyncManager = FirebaseSyncManager(application)
    private val geminiSearchRepo = GeminiSearchRepository()

    val isFirebaseAvailableState: StateFlow<Boolean> = firebaseSyncManager.isFirebaseAvailableState

    val searchGroundingState = MutableStateFlow<SearchGroundingResult?>(null)
    val isSearchGroundingLoadingState = MutableStateFlow(false)

    val mapsGroundingState = MutableStateFlow<MapsGroundingResult?>(null)
    val isMapsGroundingLoadingState = MutableStateFlow(false)

    fun performSearchGrounding(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            isSearchGroundingLoadingState.value = true
            val result = geminiSearchRepo.searchWithGrounding(query)
            searchGroundingState.value = result
            isSearchGroundingLoadingState.value = false
        }
    }

    fun performMapsGrounding(locationQuery: String) {
        if (locationQuery.isBlank()) return
        viewModelScope.launch {
            isMapsGroundingLoadingState.value = true
            val result = geminiSearchRepo.searchWithMapsGrounding(locationQuery)
            mapsGroundingState.value = result
            isMapsGroundingLoadingState.value = false
        }
    }

    fun syncCurrentProfileToFirestore() {
        val user = firebaseUser.value
        val uid = user?.uid ?: "local_user_${System.currentTimeMillis()}"
        viewModelScope.launch {
            firebaseSyncManager.saveUserProfileToFirestore(
                uid = uid,
                name = userNameState.value,
                email = userEmailState.value,
                photoUrl = user?.photoUrl?.toString() ?: "",
                address = selectedOrigin.value?.address ?: "Metro Iloilo City"
            )
        }
    }

    val firebaseUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = authManager.getAuthStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.currentUser)

    private val database = OneGoDatabase.getDatabase(application)
    private val repository = OneGoRepository(database.historyDao(), database.savedPlaceDao())

    val historyState: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPlacesState: StateFlow<List<SavedPlaceEntity>> = repository.allSavedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated local points on top of history-generated ones
    private val _dbPoints = repository.totalPoints
    val loyaltyPointsState: StateFlow<Int> = _dbPoints
        .combine(MutableStateFlow(250)) { dbVal, initialVal ->
            (dbVal ?: 0) + initialVal
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 250)

    // Simulated wallet balance (OneGoPay Wallet)
    private val _walletBalance = MutableStateFlow(1250.0) // Start with ₱1,250.0
    val walletBalanceState: StateFlow<Double> = _walletBalance

    // Persistent user profile state
    private val prefs = application.getSharedPreferences("onego_prefs", Context.MODE_PRIVATE)

    val isGmailRegisteredState = MutableStateFlow(prefs.getBoolean("is_gmail_registered", false))
    val isGuestModeState = MutableStateFlow(prefs.getBoolean("is_guest_mode", false))
    val isProfileConfiguredState = MutableStateFlow(
        prefs.getBoolean("is_profile_configured", false) || prefs.getBoolean("is_gmail_registered", false) || prefs.getBoolean("is_guest_mode", false)
    )

    val userNameState = MutableStateFlow(prefs.getString("user_name", if (prefs.getBoolean("is_guest_mode", false)) "Guest User" else "Ka-OneGo Explorer") ?: "Ka-OneGo Explorer")
    val userEmailState = MutableStateFlow(prefs.getString("user_email", if (prefs.getBoolean("is_guest_mode", false)) "guest@onego.app" else "explorer@gmail.com") ?: "explorer@gmail.com")
    val userPhoneState = MutableStateFlow(prefs.getString("user_phone", "+63 917 123 4567") ?: "+63 917 123 4567")
    val secondaryContactState = MutableStateFlow(prefs.getString("secondary_contact", "+63 918 765 4321") ?: "+63 918 765 4321")
    val preferredContactMethodState = MutableStateFlow(prefs.getString("preferred_contact_method", "SMS & Push Notifications") ?: "SMS & Push Notifications")

    val defaultDeliveryAddressIdState = MutableStateFlow<Int?>(prefs.getInt("default_delivery_id", -1).takeIf { it != -1 })

    fun completeGmailRegistration(name: String, email: String, phone: String, address: String? = null, lat: Double = 10.7123, lng: Double = 122.5518) {
        updateProfile(name, email, phone)
        prefs.edit()
            .putBoolean("is_gmail_registered", true)
            .putBoolean("is_guest_mode", false)
            .putBoolean("is_profile_configured", true)
            .apply()
        isGmailRegisteredState.value = true
        isGuestModeState.value = false
        isProfileConfiguredState.value = true
        if (!address.isNullOrBlank()) {
            val userLocation = SavedPlaceEntity(
                name = "Home ($name)",
                address = address,
                latitude = lat,
                longitude = lng,
                iconName = "home"
            )
            viewModelScope.launch {
                repository.insertSavedPlace(userLocation)
                selectedOrigin.value = userLocation
            }
        }
    }

    fun continueAsGuest() {
        userNameState.value = "Guest User"
        userEmailState.value = "guest@onego.app"
        prefs.edit()
            .putBoolean("is_gmail_registered", false)
            .putBoolean("is_guest_mode", true)
            .putBoolean("is_profile_configured", true)
            .putString("user_name", "Guest User")
            .putString("user_email", "guest@onego.app")
            .apply()
        isGuestModeState.value = true
        isGmailRegisteredState.value = false
        isProfileConfiguredState.value = true
    }

    fun updateProfile(name: String, email: String, phone: String) {
        userNameState.value = name
        userEmailState.value = email
        userPhoneState.value = phone
        prefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_phone", phone)
            .apply()
    }

    fun updateExtendedContactInfo(
        name: String,
        email: String,
        phone: String,
        secondaryContact: String,
        preferredMethod: String
    ) {
        updateProfile(name, email, phone)
        secondaryContactState.value = secondaryContact
        preferredContactMethodState.value = preferredMethod
        prefs.edit()
            .putString("secondary_contact", secondaryContact)
            .putString("preferred_contact_method", preferredMethod)
            .apply()

        authManager.updateUserProfile(name, email) { _, _ -> }
    }

    fun setDefaultDeliveryAddress(id: Int) {
        defaultDeliveryAddressIdState.value = id
        prefs.edit().putInt("default_delivery_id", id).apply()
    }

    // Firebase Auth functions
    fun firebaseSignIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        authManager.signInWithEmail(email, pass) { success, msg ->
            if (success) {
                authManager.currentUser?.let { u ->
                    val name = u.displayName ?: email.substringBefore("@")
                    val uEmail = u.email ?: email
                    updateProfile(name, uEmail, userPhoneState.value)
                }
            }
            onResult(success, msg)
        }
    }

    fun firebaseRegister(name: String, email: String, pass: String, phone: String, onResult: (Boolean, String?) -> Unit) {
        authManager.registerWithEmail(name, email, pass) { success, msg ->
            if (success) {
                updateProfile(name, email, if (phone.isNotBlank()) phone else userPhoneState.value)
            }
            onResult(success, msg)
        }
    }

    fun firebaseAnonymousSignIn(onResult: (Boolean, String?) -> Unit) {
        authManager.signInAnonymously { success, msg ->
            if (success) {
                updateProfile("Guest Explorer", "guest@onego.app", userPhoneState.value)
            }
            onResult(success, msg)
        }
    }

    fun firebasePasswordReset(email: String, onResult: (Boolean, String?) -> Unit) {
        authManager.sendPasswordReset(email, onResult)
    }

    fun firebaseSendEmailVerification(onResult: (Boolean, String?) -> Unit) {
        authManager.sendEmailVerification(onResult)
    }

    fun firebaseSignOut() {
        authManager.signOut()
    }

    // Payment methods state
    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(
        listOf(
            PaymentMethod("pm_cash", "CASH", "Cash (On Arrival / Delivery)", "Pay with cash directly to driver", true),
            PaymentMethod("pm_card_1234", "CARD", "Visa Platinum Card", "**** **** **** 1234", false, "Ka-OneGo Explorer", "12/28"),
            PaymentMethod("pm_gcash_5678", "GCASH", "GCash Wallet", "0917 **** 5678", false, billingPhone = "09171234567")
        )
    )
    val paymentMethodsState: StateFlow<List<PaymentMethod>> = _paymentMethods

    fun addPaymentMethod(
        type: String,
        label: String,
        detail: String,
        cardholderName: String = "",
        expiryDate: String = "",
        billingPhone: String = ""
    ) {
        val newId = "pm_${type.lowercase()}_${System.currentTimeMillis()}"
        val newMethod = PaymentMethod(
            id = newId,
            type = type,
            label = label,
            detail = detail,
            isDefault = _paymentMethods.value.none { it.isDefault },
            cardholderName = cardholderName,
            expiryDate = expiryDate,
            billingPhone = billingPhone
        )
        _paymentMethods.value = _paymentMethods.value + newMethod
    }

    fun deletePaymentMethod(id: String) {
        if (id == "pm_cash") return // CASH cannot be deleted
        _paymentMethods.value = _paymentMethods.value.filter { it.id != id }
        val hasDefault = _paymentMethods.value.any { it.isDefault }
        if (!hasDefault && _paymentMethods.value.isNotEmpty()) {
            _paymentMethods.value = _paymentMethods.value.map {
                if (it.id == "pm_cash") it.copy(isDefault = true) else it
            }
        }
    }

    fun setDefaultPaymentMethod(id: String) {
        _paymentMethods.value = _paymentMethods.value.map {
            it.copy(isDefault = (it.id == id))
        }
    }

    fun topUpWallet(amount: Double) {
        _walletBalance.value += amount
    }

    fun deductWallet(amount: Double): Boolean {
        if (_walletBalance.value >= amount) {
            _walletBalance.value -= amount
            return true
        }
        return false
    }

    // Records a Direct In-App Booking/Order to the Room Database
    fun addInstantBookingToHistory(
        platformId: String,
        platformName: String,
        price: Double,
        category: String,
        pointsEarned: Int,
        originName: String = "",
        destinationName: String = ""
    ) {
        val finalOrigin = if (originName.isNotEmpty()) originName else (selectedOrigin.value?.name ?: "Current Location")
        val finalDest = if (destinationName.isNotEmpty()) destinationName else (selectedDestination.value?.name ?: "Destination")
        
        viewModelScope.launch {
            val historyEntry = HistoryEntity(
                originName = finalOrigin,
                originLat = selectedOrigin.value?.latitude ?: 0.0,
                originLng = selectedOrigin.value?.longitude ?: 0.0,
                destinationName = finalDest,
                destinationLat = selectedDestination.value?.latitude ?: 0.0,
                destinationLng = selectedDestination.value?.longitude ?: 0.0,
                category = category,
                platformId = platformId,
                fare = price,
                pointsEarned = pointsEarned
            )
            repository.insertHistory(historyEntry)
        }
    }

    // Current route search selection
    val selectedOrigin = MutableStateFlow<SavedPlaceEntity?>(null)
    val selectedDestination = MutableStateFlow<SavedPlaceEntity?>(null)

    // Mode: RIDES, FOOD, DELIVERY
    val currentMode = MutableStateFlow("RIDES")

    // Food selection state & custom stores
    val selectedRestaurantIndex = MutableStateFlow(0)
    val selectedFoodCategoryState = MutableStateFlow("All")

    private val _customRestaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val customRestaurantsState: StateFlow<List<Restaurant>> = _customRestaurants.asStateFlow()

    private val defaultRestaurants = listOf(
        // Fast Food & Meals
        Restaurant("jollibee", "Jollibee", "Filipino, Burgers, Chicken", 4.8, 120.0, "Chickenjoy 1pc Solo", "burger", listOf("Burgers & Fast Food", "Chicken & BBQ", "Rice Meals")),
        Restaurant("mang_inasal", "Mang Inasal", "Filipino BBQ, Rice Meals", 4.7, 165.0, "PM1 (Chicken Leg with Rice)", "rice", listOf("Chicken & BBQ", "Rice Meals")),
        Restaurant("mcdonalds", "McDonald's", "Burgers, Fries, Fast Food", 4.6, 149.0, "Big Mac Meal", "fries", listOf("Burgers & Fast Food")),
        Restaurant("chowking", "Chowking", "Chinese, Rice Meals, Dimsum", 4.5, 135.0, "Chao Fan with Siomai", "noodle", listOf("Noodles & Chinese", "Rice Meals")),
        Restaurant("greenwich", "Greenwich", "Pizza, Pasta, Italian", 4.4, 299.0, "Double Hawaiian Overload Pizza", "pizza", listOf("Pizza & Pasta")),
        
        // Coffee & Tea Shops
        Restaurant("starbucks", "Starbucks Coffee", "Coffee & Tea, Espresso, Pastries", 4.8, 195.0, "Iced Caramel Macchiato Venti", "coffee", listOf("Coffee & Tea", "Desserts & Bakery")),
        Restaurant("chatime", "Chatime", "Milk Tea, Coffee & Tea, Boba", 4.7, 130.0, "Pearl Milk Tea (Large)", "tea", listOf("Coffee & Tea")),
        Restaurant("coco", "CoCo Fresh Tea & Juice", "Milk Tea, Fruit Tea, Coffee & Tea", 4.8, 140.0, "3 Buddies Milk Tea", "tea", listOf("Coffee & Tea")),
        Restaurant("macao_imperial", "Macao Imperial Tea", "Milk Tea, Coffee & Tea, Cheesecake", 4.6, 155.0, "Cheesecake & Pearl Milk Tea", "tea", listOf("Coffee & Tea", "Desserts & Bakery")),
        Restaurant("dunkin", "Dunkin' Donuts", "Coffee & Tea, Donuts, Sandwiches", 4.6, 110.0, "Iced Coffee & Choco Butternut", "coffee", listOf("Coffee & Tea", "Desserts & Bakery")),
        
        // Popular Grab Stores & Specialty Restaurants
        Restaurant("bonchon", "BonChon Chicken", "Korean Fried Chicken, Rice Bowls", 4.6, 185.0, "2pc Crispy Chicken Box", "chicken", listOf("Chicken & BBQ", "Rice Meals")),
        Restaurant("army_navy", "Army Navy", "Burgers, Burritos, Mexican", 4.7, 260.0, "Bully Boy Burger & Fries", "burger", listOf("Burgers & Fast Food")),
        Restaurant("shakeys", "Shakey's Pizza", "Pizza, Chicken, Mojos", 4.5, 380.0, "Thin Crust Manager's Choice", "pizza", listOf("Pizza & Pasta", "Chicken & BBQ")),
        Restaurant("yellow_cab", "Yellow Cab Pizza Co.", "NY-Style Pizza, Pasta, Wings", 4.6, 420.0, "New York's Finest Pizza", "pizza", listOf("Pizza & Pasta", "Chicken & BBQ")),
        Restaurant("potato_corner", "Potato Corner", "Flavored Fries, Snacks", 4.8, 95.0, "Tera Fries Sour Cream & BBQ", "fries", listOf("Burgers & Fast Food")),
        Restaurant("goldilocks", "Goldilocks", "Bakery, Cakes, Meals", 4.5, 160.0, "Fresh Lumpia & Dinuguan", "cake", listOf("Desserts & Bakery", "Rice Meals")),
        Restaurant("red_ribbon", "Red Ribbon", "Cakes, Pastries, Filipino", 4.6, 175.0, "Black Forest Mini Cake", "cake", listOf("Desserts & Bakery"))
    )

    val restaurants: List<Restaurant>
        get() = _customRestaurants.value + defaultRestaurants

    val allRestaurantsState: StateFlow<List<Restaurant>> = _customRestaurants.map { custom ->
        custom + defaultRestaurants
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultRestaurants)

    fun addCustomStore(
        name: String,
        cuisines: String,
        sampleItemName: String,
        itemPrice: Double,
        selectedCategories: List<String>,
        imageUri: String?,
        address: String = "Metro Iloilo City",
        latitude: Double = 10.7123,
        longitude: Double = 122.5518
    ) {
        val newStore = Restaurant(
            id = "custom_store_${System.currentTimeMillis()}",
            name = name,
            cuisines = cuisines,
            rating = 5.0,
            itemPrice = itemPrice,
            sampleItemName = sampleItemName,
            iconResId = "store",
            categories = selectedCategories,
            imageUri = imageUri,
            isUserStore = true,
            address = address,
            latitude = latitude,
            longitude = longitude
        )
        _customRestaurants.value = listOf(newStore) + _customRestaurants.value
        selectedRestaurantIndex.value = 0 // Select newly added store
        
        // Save registered store location as origin
        val storeLocation = SavedPlaceEntity(
            name = name,
            address = address,
            latitude = latitude,
            longitude = longitude,
            iconName = "shopping"
        )
        viewModelScope.launch {
            repository.insertSavedPlace(storeLocation)
            selectedOrigin.value = storeLocation
        }
    }

    // Package Delivery selection state
    val deliveryPackageTypeIndex = MutableStateFlow(0)
    val packageTypes = listOf(
        "Document / Small Envelope (up to 1kg)",
        "Standard Package / Food Box (up to 10kg)",
        "Medium Cargo Box (up to 20kg)",
        "Heavy / Bulky Furniture (up to 100kg)"
    )

    // Weather simulation to affect surge multipliers dynamically!
    val isRainingState = MutableStateFlow(false)
    val isRushHourState = MutableStateFlow(false)

    init {
        // Automatically default pickup and dropoff to make testing incredibly pleasant!
        viewModelScope.launch {
            repository.allSavedPlaces.collect { places ->
                if (places.isNotEmpty()) {
                    if (selectedOrigin.value == null) {
                        selectedOrigin.value = places.find { it.name == "Home" } ?: places.first()
                    }
                    if (selectedDestination.value == null) {
                        selectedDestination.value = places.find { it.name == "SM City Iloilo" } ?: places.getOrNull(2) ?: places.first()
                    }
                }
            }
        }
    }

    // Toggle simulated weather/hour conditions
    fun toggleRain() {
        isRainingState.value = !isRainingState.value
    }

    fun toggleRushHour() {
        isRushHourState.value = !isRushHourState.value
    }

    // Swap pickup and dropoff
    fun swapLocations() {
        val temp = selectedOrigin.value
        selectedOrigin.value = selectedDestination.value
        selectedDestination.value = temp
    }

    // Set custom locations
    private val geocodingRepo = com.example.data.repository.GoogleGeocodingRepository()

    fun updateOrigin(place: SavedPlaceEntity) {
        selectedOrigin.value = place
    }

    fun updateDestination(place: SavedPlaceEntity) {
        selectedDestination.value = place
    }

    fun updateOriginFromDeviceLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val (title, addr, _) = geocodingRepo.reverseGeocode(lat, lng)
            val placeName = if (title.isBlank() || title == "Pinned Location") "Current Device Location" else title
            selectedOrigin.value = SavedPlaceEntity(
                name = placeName,
                address = if (addr.isBlank()) "GPS Coords: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}" else addr,
                latitude = lat,
                longitude = lng,
                iconName = "gps_fixed"
            )
        }
    }

    fun updateDestinationFromDeviceLocation(lat: Double, lng: Double) {
        viewModelScope.launch {
            val (title, addr, _) = geocodingRepo.reverseGeocode(lat, lng)
            val placeName = if (title.isBlank() || title == "Pinned Location") "Pinned Delivery Point" else title
            selectedDestination.value = SavedPlaceEntity(
                name = placeName,
                address = if (addr.isBlank()) "GPS Coords: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}" else addr,
                latitude = lat,
                longitude = lng,
                iconName = "place"
            )
        }
    }

    fun selectOrigin(place: SavedPlaceEntity) {
        selectedOrigin.value = place
    }

    fun selectDestination(place: SavedPlaceEntity) {
        selectedDestination.value = place
    }

    // Scientific Haversine distance calculator between selected origin and destination
    fun getCalculatedDistanceKm(): Double {
        val origin = selectedOrigin.value ?: return 1.5
        val dest = selectedDestination.value ?: return 4.2
        return calculateDistanceKm(
            origin.latitude, origin.longitude,
            dest.latitude, dest.longitude
        )
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return if (distance.isNaN()) 1.5 else max(0.5, (distance * 10).roundToInt() / 10.0)
    }

    // Compute dynamic, beautiful rides options
    fun getComparedRides(): List<RideOption> {
        val dist = getCalculatedDistanceKm()
        val rainMultiplier = if (isRainingState.value) 1.4 else 1.0
        val rushMultiplier = if (isRushHourState.value) 1.3 else 1.0
        val totalSurge = rainMultiplier * rushMultiplier

        val pickup = selectedOrigin.value ?: SavedPlaceEntity(name="A", address="", latitude=0.0, longitude=0.0, iconName="")
        val dropoff = selectedDestination.value ?: SavedPlaceEntity(name="B", address="", latitude=0.0, longitude=0.0, iconName="")

        return listOf(
            RideOption(
                platformId = "grab",
                platformName = "Grab",
                serviceName = "GrabCar (4-Seater)",
                estimatedFare = (80.0 + dist * 15.0 + 20.0) * totalSurge,
                etaMins = max(2, (3 + dist * 1.2).toInt()),
                promoLabel = "Promo: Get ₱20 GrabUnlimited CashBack",
                badgeLabel = if (totalSurge > 1.2) "Surge Active" else "Premium Option",
                deepLinkUrl = "https://grab.sjv.io/c/ONEGO/grab-ph?subid1=user_id&subid2=onego_app"
            ),
            RideOption(
                platformId = "maxim_moto",
                platformName = "Maxim",
                serviceName = "Maxim Moto Taxi / Tricycle",
                estimatedFare = max(30.0, 30.0 + dist * 6.5), // No surge for Maxim Moto
                etaMins = max(2, (2 + dist * 0.8).toInt()),
                promoLabel = "Code: MAXIM_ONEGO for 5% off",
                badgeLabel = "Cheapest Motor",
                deepLinkUrl = "https://taximaxim.com/"
            ),
            RideOption(
                platformId = "maxim_car",
                platformName = "Maxim",
                serviceName = "Maxim Car Economy",
                estimatedFare = (65.0 + dist * 10.5) * (if (totalSurge > 1.0) 1.1 else 1.0),
                etaMins = max(3, (5 + dist * 1.5).toInt()),
                badgeLabel = "Budget Car",
                deepLinkUrl = "https://taximaxim.com/"
            ),
            RideOption(
                platformId = "xicar",
                platformName = "XiCar",
                serviceName = "XiCar Economy (No Surge)",
                estimatedFare = 50.0 + dist * 11.0, // Fixed pricing, strictly no surge!
                etaMins = max(3, (6 + dist * 1.8).toInt()),
                badgeLabel = "Featured: No Surge", // Highlighted badge from PDF!
                promoLabel = "Iloilo Local Favorite",
                deepLinkUrl = "https://xicar.app/"
            ),
            RideOption(
                platformId = "gojek",
                platformName = "Gojek",
                serviceName = "GoCar Ride",
                estimatedFare = (70.0 + dist * 13.0 + 10.0) * totalSurge,
                etaMins = max(3, (4 + dist * 1.3).toInt()),
                promoLabel = "GoPay Discount Available",
                badgeLabel = "Comfort Choice",
                deepLinkUrl = "https://onego.app/redirect?platform=gojek"
            ),
            RideOption(
                platformId = "indrive",
                platformName = "inDrive",
                serviceName = "inDrive Bidding (Suggest Fare)",
                estimatedFare = max(80.0, 100.0 + dist * 11.0), // Bargain bid
                etaMins = max(4, (7 + dist * 1.6).toInt()),
                promoLabel = "Negotiate your own fare",
                badgeLabel = "Bidding Mode",
                deepLinkUrl = "https://indrive.sjv.io/c/ONEGO/indrive-ph?subid1=rides"
            ),
            RideOption(
                platformId = "angkas",
                platformName = "Angkas",
                serviceName = "Angkas Motor Taxi",
                estimatedFare = max(50.0, (50.0 + dist * 10.0) * (if (isRainingState.value) 1.2 else 1.0)),
                etaMins = max(1, (2 + dist * 0.5).toInt()),
                promoLabel = "Angkas Shield Insured",
                badgeLabel = "Traffic Buster ⚡",
                deepLinkUrl = "angkas://ride?pickup=${pickup.latitude},${pickup.longitude}&drop=${dropoff.latitude},${dropoff.longitude}&referrer=ONEGO"
            ),
            RideOption(
                platformId = "joyride",
                platformName = "Joyride",
                serviceName = "Joyride Moto Ride",
                estimatedFare = max(45.0, (45.0 + dist * 9.0) * (if (isRainingState.value) 1.15 else 1.0)),
                etaMins = max(2, (3 + dist * 0.6).toInt()),
                promoLabel = "Cheapest Moto Ride",
                badgeLabel = "Highly Rated ⭐",
                deepLinkUrl = "https://joyride.com.ph/"
            ),
            RideOption(
                platformId = "moveit",
                platformName = "Move It",
                serviceName = "Move It Moto Taxi (Grab Powered)",
                estimatedFare = max(45.0, (48.0 + dist * 9.5) * (if (isRainingState.value) 1.15 else 1.0)),
                etaMins = max(1, (2 + dist * 0.5).toInt()),
                promoLabel = "Move It Fast & Safe",
                badgeLabel = "Top Moto Choice 🛵",
                deepLinkUrl = "https://moveit.com.ph/"
            ),
            RideOption(
                platformId = "didi",
                platformName = "DiDi",
                serviceName = "DiDi Express (China/Japan)",
                estimatedFare = (60.0 + dist * 11.5 + 15.0) * totalSurge,
                etaMins = max(3, (4 + dist * 1.1).toInt()),
                promoLabel = "Preferred Discount Active",
                badgeLabel = "Global Giant 🌏",
                deepLinkUrl = "https://onego.app/redirect?platform=didi"
            ),
            RideOption(
                platformId = "ola",
                platformName = "Ola Cabs",
                serviceName = "Ola Prime Sedan (India)",
                estimatedFare = (55.0 + dist * 10.0 + 10.0) * totalSurge,
                etaMins = max(4, (5 + dist * 1.2).toInt()),
                promoLabel = "Ola Share Code: ASIA5",
                badgeLabel = "South Asia Favorite",
                deepLinkUrl = "https://onego.app/redirect?platform=ola"
            ),
            RideOption(
                platformId = "tada",
                platformName = "TADA",
                serviceName = "TADA Zero-Commission Car",
                estimatedFare = 50.0 + dist * 12.0,
                etaMins = max(3, (5 + dist * 1.4).toInt()),
                promoLabel = "Singapore/Vietnam Active",
                badgeLabel = "Zero Surge Fee 💎",
                deepLinkUrl = "https://onego.app/redirect?platform=tada"
            ),
            RideOption(
                platformId = "kakaot",
                platformName = "Kakao T",
                serviceName = "Kakao Taxi (South Korea)",
                estimatedFare = (75.0 + dist * 14.0 + 25.0) * totalSurge,
                etaMins = max(2, (3 + dist * 1.0).toInt()),
                promoLabel = "Kakao Blue Premium Fleet",
                badgeLabel = "East Asia Elite",
                deepLinkUrl = "https://onego.app/redirect?platform=kakaot"
            ),
            RideOption(
                platformId = "lineman_taxi",
                platformName = "LINE MAN",
                serviceName = "LINE MAN Taxi (Thailand)",
                estimatedFare = (45.0 + dist * 9.5) * totalSurge,
                etaMins = max(3, (4 + dist * 1.1).toInt()),
                promoLabel = "Rabbit LINE Pay: 10% Off",
                badgeLabel = "Bangkok Direct 🇹🇭",
                deepLinkUrl = "https://onego.app/redirect?platform=lineman"
            )
        ).sortedBy { it.estimatedFare }
    }

    // Compute dynamic, beautiful Food delivery options
    fun getComparedFood(): List<FoodOption> {
        val dist = getCalculatedDistanceKm()
        val rest = restaurants.getOrNull(selectedRestaurantIndex.value) ?: restaurants.first()
        val rainMultiplier = if (isRainingState.value) 1.25 else 1.0
        val rushMultiplier = if (isRushHourState.value) 1.15 else 1.0
        val totalSurge = rainMultiplier * rushMultiplier

        return listOf(
            FoodOption(
                platformId = "grabfood",
                platformName = "GrabFood",
                basePrice = rest.itemPrice,
                deliveryFee = max(29.0, (49.0 + dist * 5.0) * totalSurge),
                serviceFee = 15.0,
                estimatedTotal = rest.itemPrice + max(29.0, (49.0 + dist * 5.0) * totalSurge) + 15.0 - 30.0, // Promo included
                etaMins = max(15, (25 + dist * 2.5).toInt()),
                discountLabel = "GrabUnlimited: ₱30 Off delivery!",
                badgeLabel = "Broadest Choice",
                deepLinkUrl = "https://grab.sjv.io/c/ONEGO/grab-ph?subid1=user_id&subid2=onego_app"
            ),
            FoodOption(
                platformId = "foodpanda",
                platformName = "Foodpanda",
                basePrice = rest.itemPrice,
                deliveryFee = max(19.0, (39.0 + dist * 4.0) * totalSurge),
                serviceFee = 10.0,
                estimatedTotal = rest.itemPrice + max(19.0, (39.0 + dist * 4.0) * totalSurge) + 10.0 - 25.0,
                etaMins = max(15, (20 + dist * 2.0).toInt()),
                discountLabel = "pandaPro: Free delivery over ₱299",
                badgeLabel = "Fastest Delivery 🚀",
                deepLinkUrl = "https://www.jdoqocy.com/click-ONEGO-foodpanda-ph"
            ),
            FoodOption(
                platformId = "shopeefood",
                platformName = "ShopeeFood",
                basePrice = rest.itemPrice,
                deliveryFee = max(9.0, (29.0 + dist * 3.0) * totalSurge),
                serviceFee = 5.0,
                estimatedTotal = rest.itemPrice + max(9.0, (29.0 + dist * 3.0) * totalSurge) + 5.0 - 15.0,
                etaMins = max(20, (30 + dist * 3.0).toInt()),
                discountLabel = "ShopeePay: 30% discount voucher!",
                badgeLabel = "Lowest Service Fee",
                deepLinkUrl = "https://onego.app/redirect?platform=shopeefood"
            ),
            FoodOption(
                platformId = "gojekfood",
                platformName = "Gojek Food",
                basePrice = rest.itemPrice,
                deliveryFee = max(15.0, (35.0 + dist * 3.5) * totalSurge),
                serviceFee = 8.0,
                estimatedTotal = rest.itemPrice + max(15.0, (35.0 + dist * 3.5) * totalSurge) + 8.0 - 20.0,
                etaMins = max(18, (27 + dist * 2.2).toInt()),
                discountLabel = "GoPay Voucher: ₱20 Off",
                badgeLabel = "Loyalty Friendly",
                deepLinkUrl = "https://onego.app/redirect?platform=gojek"
            ),
            FoodOption(
                platformId = "lineman_food",
                platformName = "LINE MAN Food",
                basePrice = rest.itemPrice,
                deliveryFee = max(12.0, (25.0 + dist * 3.2) * totalSurge),
                serviceFee = 6.0,
                estimatedTotal = rest.itemPrice + max(12.0, (25.0 + dist * 3.2) * totalSurge) + 6.0 - 15.0,
                etaMins = max(15, (22 + dist * 2.1).toInt()),
                discountLabel = "LINE MAN Super Deals: 15% Off",
                badgeLabel = "Thailand #1 🍜",
                deepLinkUrl = "https://onego.app/redirect?platform=lineman"
            ),
            FoodOption(
                platformId = "baemin",
                platformName = "Baemin",
                basePrice = rest.itemPrice,
                deliveryFee = max(18.0, (32.0 + dist * 3.8) * totalSurge),
                serviceFee = 8.0,
                estimatedTotal = rest.itemPrice + max(18.0, (32.0 + dist * 3.8) * totalSurge) + 8.0 - 20.0,
                etaMins = max(14, (18 + dist * 1.8).toInt()),
                discountLabel = "Baemin Fast-Track coupon",
                badgeLabel = "Korea's Choice 🇰🇷",
                deepLinkUrl = "https://onego.app/redirect?platform=baemin"
            ),
            FoodOption(
                platformId = "deliveroo",
                platformName = "Deliveroo SG/HK",
                basePrice = rest.itemPrice,
                deliveryFee = max(22.0, (42.0 + dist * 4.5) * totalSurge),
                serviceFee = 12.0,
                estimatedTotal = rest.itemPrice + max(22.0, (42.0 + dist * 4.5) * totalSurge) + 12.0 - 25.0,
                etaMins = max(16, (23 + dist * 2.3).toInt()),
                discountLabel = "Deliveroo Plus: Free over SG$25",
                badgeLabel = "Premium Selection",
                deepLinkUrl = "https://onego.app/redirect?platform=deliveroo"
            ),
            FoodOption(
                platformId = "meituan",
                platformName = "Meituan / KeeTa",
                basePrice = rest.itemPrice,
                deliveryFee = max(10.0, (20.0 + dist * 3.0) * totalSurge),
                serviceFee = 5.0,
                estimatedTotal = rest.itemPrice + max(10.0, (20.0 + dist * 3.0) * totalSurge) + 5.0 - 30.0,
                etaMins = max(12, (15 + dist * 1.5).toInt()),
                discountLabel = "KeeTa Member Welcome: Big Savings",
                badgeLabel = "Hyper-Speed ⚡",
                deepLinkUrl = "https://onego.app/redirect?platform=meituan"
            ),
            FoodOption(
                platformId = "wolt",
                platformName = "Wolt Japan",
                basePrice = rest.itemPrice,
                deliveryFee = max(25.0, (45.0 + dist * 4.2) * totalSurge),
                serviceFee = 15.0,
                estimatedTotal = rest.itemPrice + max(25.0, (45.0 + dist * 4.2) * totalSurge) + 15.0 - 10.0,
                etaMins = max(18, (26 + dist * 2.4).toInt()),
                discountLabel = "Wolt Promo: First Order Discount",
                badgeLabel = "Nordic/Japan Quality",
                deepLinkUrl = "https://onego.app/redirect?platform=wolt"
            )
        ).sortedBy { it.estimatedTotal }
    }

    // Compute dynamic Logistics/Delivery options
    fun getComparedDelivery(): List<DeliveryOption> {
        val dist = getCalculatedDistanceKm()
        val typeIdx = deliveryPackageTypeIndex.value
        val rainMultiplier = if (isRainingState.value) 1.2 else 1.0
        val rushMultiplier = if (isRushHourState.value) 1.15 else 1.0
        val totalSurge = rainMultiplier * rushMultiplier

        val pickup = selectedOrigin.value ?: SavedPlaceEntity(name="A", address="", latitude=0.0, longitude=0.0, iconName="")
        val dropoff = selectedDestination.value ?: SavedPlaceEntity(name="B", address="", latitude=0.0, longitude=0.0, iconName="")

        return listOf(
            DeliveryOption(
                platformId = "grabexpress",
                platformName = "GrabExpress",
                serviceName = "GrabExpress Instant (Motorcycle)",
                estimatedFare = (60.0 + dist * 12.0) * totalSurge,
                etaMins = max(10, (15 + dist * 1.5).toInt()),
                maxWeightKg = 20.0,
                badgeLabel = "High Security (Insured ₱10k)",
                deepLinkUrl = "https://grab.sjv.io/c/ONEGO/grab-ph?subid1=user_id&subid2=onego_app"
            ),
            DeliveryOption(
                platformId = "lalamove_moto",
                platformName = "Lalamove",
                serviceName = "Lalamove Motorbike Instant",
                estimatedFare = (49.0 + dist * 8.5) * totalSurge,
                etaMins = max(10, (12 + dist * 1.2).toInt()),
                maxWeightKg = 20.0,
                badgeLabel = "PH Logistics Leader 🏆",
                deepLinkUrl = "https://web.lalamove.com/place/order?slat=${pickup.latitude}&slng=${pickup.longitude}&elat=${dropoff.latitude}&elng=${dropoff.longitude}&refer=ONEGO"
            ),
            DeliveryOption(
                platformId = "lalamove_car",
                platformName = "Lalamove",
                serviceName = "Lalamove Sedan MPV Delivery",
                estimatedFare = (150.0 + dist * 15.0) * (if (isRainingState.value) 1.1 else 1.0),
                etaMins = max(15, (22 + dist * 1.8).toInt()),
                maxWeightKg = 300.0,
                badgeLabel = "Bulk/Large Deliveries",
                deepLinkUrl = "https://web.lalamove.com/place/order?slat=${pickup.latitude}&slng=${pickup.longitude}&elat=${dropoff.latitude}&elng=${dropoff.longitude}&refer=ONEGO"
            ),
            DeliveryOption(
                platformId = "maxim_delivery",
                platformName = "Maxim",
                serviceName = "Maxim Express Delivery",
                estimatedFare = max(35.0, 35.0 + dist * 6.0), // No surge
                etaMins = max(10, (14 + dist * 1.4).toInt()),
                maxWeightKg = 15.0,
                badgeLabel = "Cheapest Delivery 💰",
                deepLinkUrl = "https://taximaxim.com/"
            ),
            DeliveryOption(
                platformId = "gojek_logistics",
                platformName = "Gojek",
                serviceName = "GoSend Delivery",
                estimatedFare = (55.0 + dist * 10.0) * totalSurge,
                etaMins = max(10, (14 + dist * 1.3).toInt()),
                maxWeightKg = 20.0,
                badgeLabel = "Reliable Tracking",
                deepLinkUrl = "https://onego.app/redirect?platform=gojek"
            ),
            DeliveryOption(
                platformId = "ninjavan",
                platformName = "Ninja Van",
                serviceName = "Ninja Van Standard Express (SEA)",
                estimatedFare = (40.0 + dist * 7.0) * totalSurge,
                etaMins = max(15, (25 + dist * 1.8).toInt()),
                maxWeightKg = 30.0,
                badgeLabel = "SEA Wide Network 🥷",
                deepLinkUrl = "https://onego.app/redirect?platform=ninjavan"
            ),
            DeliveryOption(
                platformId = "jtexpress",
                platformName = "J&T Express",
                serviceName = "J&T Express (Next-Day)",
                estimatedFare = (38.0 + dist * 6.5) * totalSurge,
                etaMins = max(15, (30 + dist * 2.0).toInt()),
                maxWeightKg = 50.0,
                badgeLabel = "Largest SEA Logistics",
                deepLinkUrl = "https://onego.app/redirect?platform=jtexpress"
            ),
            DeliveryOption(
                platformId = "deliveree",
                platformName = "Deliveree",
                serviceName = "Deliveree Heavy Cargo Fleet",
                estimatedFare = (180.0 + dist * 18.0) * (if (isRainingState.value) 1.1 else 1.0),
                etaMins = max(20, (30 + dist * 2.5).toInt()),
                maxWeightKg = 2000.0,
                badgeLabel = "Mega Cargo 🚚",
                deepLinkUrl = "https://onego.app/redirect?platform=deliveree"
            ),
            DeliveryOption(
                platformId = "yamato",
                platformName = "Yamato TA-Q-BIN",
                serviceName = "Yamato Black Cat (JP/TW/SG)",
                estimatedFare = (50.0 + dist * 9.0) * totalSurge,
                etaMins = max(12, (18 + dist * 1.4).toInt()),
                maxWeightKg = 25.0,
                badgeLabel = "Japanese Precision 🐈",
                deepLinkUrl = "https://onego.app/redirect?platform=yamato"
            )
        ).filter {
            // Filter options based on weight capacity compared to selection
            when (typeIdx) {
                0 -> true // Up to 1kg (all support)
                1 -> true // Up to 10kg (all support except those that are too small, but here all support at least 15kg)
                2 -> it.maxWeightKg >= 20.0 // Medium cargo box (excludes Maxim Express at 15kg)
                3 -> it.maxWeightKg >= 300.0 // Heavy bulky furniture (only Lalamove Sedan MPV and Deliveree Heavy Cargo Fleet)
                else -> true
            }
        }.sortedBy { it.estimatedFare }
    }

    fun getComparedPabili(itemsTotalCost: Double = 0.0): List<PabiliOption> {
        val dist = getCalculatedDistanceKm()
        val rainMul = if (isRainingState.value) 1.20 else 1.0
        val rushMul = if (isRushHourState.value) 1.15 else 1.0
        val factor = rainMul * rushMul

        return listOf(
            PabiliOption(
                platformId = "grab_pabili",
                platformName = "Grab Pabili",
                serviceName = "GrabExpress Pabili (Buy For Me)",
                deliveryFee = (60.0 + dist * 12.0) * factor,
                serviceFee = 25.0,
                totalDeliveryAndServiceFee = ((60.0 + dist * 12.0) * factor) + 25.0,
                etaMins = max(10, (15 + dist * 2.0).toInt()),
                maxShoppingBudget = 2000.0,
                badgeLabel = "Grab Express 🛍️",
                promoLabel = "Most Reliable Driver Fleet",
                deepLinkUrl = "https://www.grab.com/ph/express/"
            ),
            PabiliOption(
                platformId = "joyride_pabili",
                platformName = "Joyride Pabili",
                serviceName = "Joyride Super Pabili",
                deliveryFee = (50.0 + dist * 10.0) * factor,
                serviceFee = 20.0,
                totalDeliveryAndServiceFee = ((50.0 + dist * 10.0) * factor) + 20.0,
                etaMins = max(12, (18 + dist * 2.0).toInt()),
                maxShoppingBudget = 2000.0,
                badgeLabel = "Popular Moto Choice 🛵",
                promoLabel = "Low Service Fee",
                deepLinkUrl = "https://joyride.com.ph/"
            ),
            PabiliOption(
                platformId = "maxim_pabili",
                platformName = "Maxim Pabili",
                serviceName = "Maxim Buy & Deliver",
                deliveryFee = (40.0 + dist * 8.5) * factor,
                serviceFee = 15.0,
                totalDeliveryAndServiceFee = ((40.0 + dist * 8.5) * factor) + 15.0,
                etaMins = max(15, (22 + dist * 2.5).toInt()),
                maxShoppingBudget = 2000.0,
                badgeLabel = "Cheapest Pabili 💰",
                promoLabel = "Lowest Starting Rate",
                deepLinkUrl = "https://taximaxim.com/ph/"
            ),
            PabiliOption(
                platformId = "lalamove_pabili",
                platformName = "Lalamove Purchase",
                serviceName = "Lalamove Purchase Service",
                deliveryFee = (65.0 + dist * 11.0) * factor,
                serviceFee = 30.0,
                totalDeliveryAndServiceFee = ((65.0 + dist * 11.0) * factor) + 30.0,
                etaMins = max(8, (12 + dist * 1.5).toInt()),
                maxShoppingBudget = 2000.0,
                badgeLabel = "Fastest Pabili ⚡",
                promoLabel = "Express Priority Dispatch",
                deepLinkUrl = "https://web.lalamove.com/"
            ),
            PabiliOption(
                platformId = "moveit_pabili",
                platformName = "Move It Pabili",
                serviceName = "Move It Express Pabili",
                deliveryFee = (48.0 + dist * 9.5) * factor,
                serviceFee = 20.0,
                totalDeliveryAndServiceFee = ((48.0 + dist * 9.5) * factor) + 20.0,
                etaMins = max(10, (16 + dist * 2.0).toInt()),
                maxShoppingBudget = 2000.0,
                badgeLabel = "Grab Powered 🛵",
                promoLabel = "Fast & Safe Moto Pabili",
                deepLinkUrl = "https://moveit.com.ph/"
            )
        )
    }

    // Launch affiliate link and save to Room history
    fun launchPlatform(
        context: Context,
        platformId: String,
        platformName: String,
        price: Double,
        customDeepLinkUrl: String? = null
    ) {
        val origin = selectedOrigin.value ?: return
        val dest = selectedDestination.value ?: return

        // 1. Save history to Room Database which reactive triggers totalPointsFlow and adds loyalty points!
        viewModelScope.launch {
            val historyEntry = HistoryEntity(
                originName = origin.name,
                originLat = origin.latitude,
                originLng = origin.longitude,
                destinationName = dest.name,
                destinationLat = dest.latitude,
                destinationLng = dest.longitude,
                category = currentMode.value,
                platformId = platformId,
                fare = price,
                pointsEarned = 15 // Standard referral callback points!
            )
            repository.insertHistory(historyEntry)
        }

        // 2. Generate direct tracked URL
        val validWebUrl = getValidWebUrlForPlatform(platformId, origin, dest)
        val targetUrl = if (!customDeepLinkUrl.isNullOrBlank()) customDeepLinkUrl else validWebUrl

        // 3. Launch system browser or deep link
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(
                context,
                "Opening $platformName! (${origin.name} ➔ ${dest.name}). Earning +15 OneGo Points!",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Fallback to official web portal or Google Maps route
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(validWebUrl))
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
                Toast.makeText(
                    context,
                    "Opening $platformName Web Portal! (${origin.name} ➔ ${dest.name}). +15 Points!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (ex: Exception) {
                Toast.makeText(
                    context,
                    "Selected $platformName: Route from ${origin.name} to ${dest.name} (₱${"%.2f".format(price)}). +15 Points!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Map provider ID to verified, official web portal / route URL
     */
    fun getValidWebUrlForPlatform(platformId: String, origin: SavedPlaceEntity, dest: SavedPlaceEntity): String {
        return when (platformId) {
            "grab", "grabfood", "grabexpress" -> "https://www.grab.com/ph/"
            "maxim_moto", "maxim_car", "maxim_delivery" -> "https://taximaxim.com/"
            "xicar" -> "https://xicar.app/"
            "gojek", "gojekfood", "gojek_logistics" -> "https://www.gojek.com/"
            "indrive" -> "https://indrive.com/"
            "angkas" -> "https://www.angkas.com/"
            "joyride" -> "https://joyride.com.ph/"
            "moveit" -> "https://moveit.com.ph/"
            "foodpanda" -> "https://www.foodpanda.ph/"
            "shopeefood" -> "https://shopee.ph/m/shopeefood"
            "lalamove_moto", "lalamove_car" -> "https://web.lalamove.com/"
            "didi" -> "https://www.didiglobal.com/"
            "ola" -> "https://www.olacabs.com/"
            "tada" -> "https://tada.global/"
            "kakaot" -> "https://www.kakaocorp.com/"
            "lineman", "lineman_taxi" -> "https://lineman.line.me/"
            "ninjavan" -> "https://www.ninjavan.co/en-ph"
            "jtexpress" -> "https://www.jtexpress.ph/"
            "deliveree" -> "https://www.deliveree.com/ph/"
            "yamato" -> "https://www.yamato-hd.co.jp/english/"
            "baemin" -> "https://www.baemin.com/"
            "deliveroo" -> "https://deliveroo.co.uk/"
            "meituan" -> "https://www.meituan.com/"
            "wolt" -> "https://wolt.com/"
            else -> "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}"
        }
    }

    // Add a custom user location to saved places
    fun addSavedPlace(name: String, address: String, lat: Double, lng: Double, iconName: String) {
        viewModelScope.launch {
            repository.insertSavedPlace(
                SavedPlaceEntity(
                    name = name,
                    address = address,
                    latitude = lat,
                    longitude = lng,
                    iconName = iconName
                )
            )
        }
    }

    // Delete a saved place
    fun deleteSavedPlace(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedPlaceById(id)
        }
    }

    // Clear search history log
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
