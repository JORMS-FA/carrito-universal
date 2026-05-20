package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ProductEntity
import com.example.data.repository.ProductRepository
import com.example.util.NotificationHelper
import com.example.util.SessionManager
import com.example.util.UserSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ExtractionUiState {
    object Idle : ExtractionUiState()
    object Loading : ExtractionUiState()
    data class Success(val product: ProductEntity) : ExtractionUiState()
    data class Error(val message: String) : ExtractionUiState()
}

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    
    val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProductRepository(database.productDao())

    // Theme States (One UI 8.5 customization)
    private val _themeMode = MutableStateFlow(sessionManager.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _themeColor = MutableStateFlow(sessionManager.getThemeColor())
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    fun updateTheme(mode: String, color: String) {
        sessionManager.setTheme(mode, color)
        _themeMode.value = mode
        _themeColor.value = color
    }

    // Active User State
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    // Full User Products Flow
    val allUserProducts: Flow<List<ProductEntity>> = _currentUser.flatMapLatest { session ->
        if (session != null) {
            repository.getProductsByUserId(session.email)
        } else {
            flowOf(emptyList())
        }
    }

    // List of Unique Categories
    val uniqueCategories: Flow<List<String>> = _currentUser.flatMapLatest { session ->
        if (session != null) {
            repository.getUniqueCategories(session.email)
        } else {
            flowOf(emptyList())
        }
    }

    // Upcoming reminders list
    val reminderProducts: Flow<List<ProductEntity>> = _currentUser.flatMapLatest { session ->
        if (session != null) {
            repository.getProductsWithReminders(session.email)
        } else {
            flowOf(emptyList())
        }
    }

    // Active extraction trigger state
    private val _extractionState = MutableStateFlow<ExtractionUiState>(ExtractionUiState.Idle)
    val extractionState: StateFlow<ExtractionUiState> = _extractionState.asStateFlow()

    init {
        // Initialize notifications and load session
        NotificationHelper.createNotificationChannel(application)
        checkActiveSession()
    }

    fun checkActiveSession() {
        _currentUser.value = sessionManager.getUserSession()
    }

    fun logout() {
        sessionManager.logout()
        _currentUser.value = null
    }

    fun loginMock(email: String, name: String, photo: String) {
        sessionManager.login(email, name, photo)
        checkActiveSession()
    }

    // --- Product DB Operations ---

    fun saveProduct(product: ProductEntity, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            var productToSave = product.copy(userId = user.email)
            
            // Initialize price history if blank
            if (productToSave.priceHistory.isBlank()) {
                productToSave = productToSave.copy(priceHistory = "${System.currentTimeMillis()}:${productToSave.price}")
            }
            
            // Check alert threshold instantly on creation
            if (productToSave.priceAlertEnabled && productToSave.price < productToSave.priceAlertThreshold) {
                productToSave = productToSave.copy(status = "Precio bajo detectado")
                NotificationHelper.triggerImmediateNotification(
                    context = getApplication(),
                    notificationId = productToSave.hashCode(),
                    title = "¡Alerta de Precio! 💸",
                    message = "El precio de ${productToSave.title} bajó a ${productToSave.currency}${productToSave.price}, menor a tu alerta de ${productToSave.currency}${productToSave.priceAlertThreshold}!"
                )
            }
            
            val insertId = repository.insertProduct(productToSave)
            onComplete(insertId.toInt())
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            var productToSave = product
            
            // Initialize price history if blank
            if (productToSave.priceHistory.isBlank()) {
                productToSave = productToSave.copy(priceHistory = "${System.currentTimeMillis()}:${productToSave.price}")
            } else {
                // Check if current price is different from last recorded in history, if so append it
                val historyParts = productToSave.priceHistory.split(",")
                val lastPart = historyParts.lastOrNull()
                val lastPriceVal = lastPart?.split(":")?.getOrNull(1)?.toDoubleOrNull()
                if (lastPriceVal != null && lastPriceVal != productToSave.price) {
                    productToSave = productToSave.copy(
                        priceHistory = "${productToSave.priceHistory},${System.currentTimeMillis()}:${productToSave.price}"
                    )
                }
            }
            
            // Verify price alert threshold
            if (productToSave.priceAlertEnabled && productToSave.price < productToSave.priceAlertThreshold && productToSave.status != "Precio bajo detectado") {
                productToSave = productToSave.copy(status = "Precio bajo detectado")
                NotificationHelper.triggerImmediateNotification(
                    context = getApplication(),
                    notificationId = productToSave.id,
                    title = "¡Alerta de Precio! 💸",
                    message = "El precio de ${productToSave.title} bajó a ${productToSave.currency}${productToSave.price}, menor a tu alerta de ${productToSave.currency}${productToSave.priceAlertThreshold}!"
                )
            } else if (productToSave.status == "Precio bajo detectado" && (!productToSave.priceAlertEnabled || productToSave.price >= productToSave.priceAlertThreshold)) {
                // Reset status to "Por revisar" if price alert is disabled or price went back up details
                productToSave = productToSave.copy(status = "Por revisar")
            }
            
            repository.updateProduct(productToSave)
            
            // Adjust alarms if reminderDate changed
            if (productToSave.reminderDate != null && productToSave.status != "Comprado" && productToSave.status != "Descartado") {
                val formattedPrice = "${productToSave.currency}${productToSave.price}"
                NotificationHelper.scheduleReminder(
                    context = getApplication(),
                    productId = productToSave.id,
                    timeInMillis = productToSave.reminderDate,
                    title = "ShopWise: Decisión para ${productToSave.title}",
                    message = "Programaste evaluar comprar ${productToSave.title} en ${productToSave.sourceStore} por $formattedPrice. ¿Lo compramos hoy?"
                )
            } else {
                NotificationHelper.cancelReminder(getApplication(), productToSave.id)
            }
        }
    }

    fun simulateNewPrice(product: ProductEntity, newPrice: Double) {
        viewModelScope.launch {
            val currentTimestamp = System.currentTimeMillis()
            val cleanHistory = if (product.priceHistory.isBlank()) {
                "${product.createdAt}:${product.price}"
            } else {
                product.priceHistory
            }
            val newHistory = "$cleanHistory,$currentTimestamp:$newPrice"
            var updated = product.copy(
                price = newPrice,
                priceHistory = newHistory,
                updatedAt = currentTimestamp
            )
            
            // Check alert threshold
            if (updated.priceAlertEnabled && newPrice < updated.priceAlertThreshold) {
                updated = updated.copy(status = "Precio bajo detectado")
                NotificationHelper.triggerImmediateNotification(
                    context = getApplication(),
                    notificationId = updated.id,
                    title = "¡Alerta de Precio! 🔥",
                    message = "¡Detectamos rebaja para ${updated.title}! El precio bajó a ${updated.currency}$newPrice (umbral de alerta ${updated.currency}${updated.priceAlertThreshold})"
                )
            } else if (updated.status == "Precio bajo detectado" && (!updated.priceAlertEnabled || newPrice >= updated.priceAlertThreshold)) {
                updated = updated.copy(status = "Por revisar")
            }
            repository.updateProduct(updated)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), product.id)
            repository.deleteProduct(product)
        }
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        return repository.getProductById(id)
    }

    suspend fun getProductsByCategorySync(userId: String, category: String): List<ProductEntity> {
        return repository.getProductsByCategorySync(userId, category)
    }

    // --- Gemini Web Link Analysis ---

    fun extractAndAnalyzeLink(url: String) {
        if (url.isBlank()) {
            _extractionState.value = ExtractionUiState.Error("Ingresa una URL de producto válida.")
            return
        }
        
        _extractionState.value = ExtractionUiState.Loading
        viewModelScope.launch {
            try {
                val user = _currentUser.value
                val userEmail = user?.email ?: "anonymous@shopwise.com"
                
                val result = repository.extractProductFromUrl(url)
                
                // Formulate target entity
                val finalProduct = ProductEntity(
                    userId = userEmail,
                    title = result.title,
                    url = url,
                    brand = result.brand,
                    imageUrl = result.imageUrl,
                    price = result.price,
                    currency = result.currency,
                    category = result.category,
                    priority = "Media", // Default mid-level
                    status = "Por revisar", // Suggested initially
                    reminderDate = null,
                    rating = result.rating,
                    summaryMd = result.summaryMd,
                    notes = "",
                    sourceStore = result.sourceStore,
                    comparisonGroupId = null,
                    quality = result.quality,
                    pros = result.pros,
                    contras = result.contras,
                    score = result.score
                )
                
                _extractionState.value = ExtractionUiState.Success(finalProduct)
            } catch (e: Exception) {
                Log.e("ShoppingViewModel", "Failed link analysis: ${e.message}", e)
                _extractionState.value = ExtractionUiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al analizar el producto.")
            }
        }
    }

    fun clearExtractionState() {
        _extractionState.value = ExtractionUiState.Idle
    }

    // --- Reminder Snooze Controller ---

    fun snoozeReminder(product: ProductEntity, addHours: Int) {
        val nextTime = System.currentTimeMillis() + (addHours * 60 * 60 * 1000L)
        val updated = product.copy(
            reminderDate = nextTime,
            updatedAt = System.currentTimeMillis()
        )
        updateProduct(updated)
    }

    fun clearReminder(product: ProductEntity) {
        val updated = product.copy(
            reminderDate = null,
            updatedAt = System.currentTimeMillis()
        )
        updateProduct(updated)
    }
}
