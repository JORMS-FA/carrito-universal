package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ProductDao
import com.example.data.local.ProductEntity
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProductRepository(private val productDao: ProductDao) {

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts("") // Empty string for default user or we can filter dynamically

    fun getProductsByUserId(userId: String): Flow<List<ProductEntity>> {
        return productDao.getAllProducts(userId)
    }

    fun getProductsByCategory(userId: String, category: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(userId, category)
    }

    suspend fun getProductsByCategorySync(userId: String, category: String): List<ProductEntity> {
        return productDao.getProductsByCategorySync(userId, category)
    }

    fun getUniqueCategories(userId: String): Flow<List<String>> {
        return productDao.getUniqueCategories(userId)
    }

    fun getProductsWithReminders(userId: String): Flow<List<ProductEntity>> {
        return productDao.getProductsWithReminders(userId)
    }

    suspend fun getProductById(id: Int): ProductEntity? {
        return productDao.getProductById(id)
    }

    suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    /**
     * Utilizes Gemini 3.5-flash via REST API to extract product data from URL.
     */
    suspend fun extractProductFromUrl(url: String): ProductExtractionResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w("ProductRepository", "Gemini API key is not configured in Secrets panel")
            return@withContext getLocalFallbackResult(url, "Falta clave Gemini API. Configúrala en el panel de Secrets de AI Studio en la nube.")
        }

        val prompt = """
            Eres un analista experto de productos de compras online. 
            Analiza en detalle este enlace de producto: $url

            Obtén información o deduce especificaciones precisas según tus conocimientos globales de la tienda y el producto mencionado.
            Debes devolver EXCLUSIVAMENTE un bloque de texto en formato JSON puro (sin bloques decorativos markdown ```json o similares, SOLO el JSON plano) que calce exactamente con este esquema:
            
            {
              "title": "Nombre del producto de forma clara y concisa",
              "brand": "Marca o fabricante",
              "price": 129.99 (número decimal, usa 0.0 si es desconocido),
              "currency": "$", "USD", "EUR", "MXN", "COP" o el correspondiente del mercado,
              "category": "Tecnología", "Hogar", "Ropa", "Deportes", "Belleza", "Libros" u "Otros" (elige uno de estos valores),
              "description": "Una descripción premium corta de 1 a 2 párrafos",
              "sourceStore": "La tienda correspondiente, ej. Amazon, Mercado Libre, Temu, AliExpress, Shopee, eBay, Nike",
              "imageUrl": "Elige el enlace de Unsplash de alta resolución que calce mejor de estos ejemplos para verse espectacular:
                 - Auriculares/Sonido: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e'
                 - Laptops/Monitores: 'https://images.unsplash.com/photo-1496181130204-755241524eab'
                 - Celulares/Tech: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9'
                 - Relojes/Gadgets: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30'
                 - Zapatillas/Zapatos: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff' o 'https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a'
                 - Ropa/Moda: 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105'
                 - Hogar/Decoración: 'https://images.unsplash.com/photo-1513694203232-719a280e022f'
                 - Café/Tazas: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd'
                 - Libros: 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6'
                 Si no calza con ninguno de estos ejemplos, puedes colocar una búsqueda temática o usar el de gadgets",
              "rating": 4.5 (estimada 1.0 - 5.0),
              "summaryMd": "# Ficha del Producto\n\n![Banner](enlace de imageUrl)\n\n## Resumen Ejecutivo\nEste producto es una opción de compra analizada mediante inteligencia artificial. Proporciona una experiencia de uso excelente y destaca en su categoría.\n\n## Datos Claves\n- **Tienda**: Tienda especificada en sourceStore\n- **Precio**: precio estimado\n- **Calificación**: calificación promedio\n\n## Pros y Contras\n### Pros\n1. Relación calidad-precio balanceada.\n2. Diseño moderno y sofisticado.\n### Contras\n1. Disponibilidad de envío variable según locación.\n\n## Notas del Analista\nRecomendado para compra inmediata si buscas el balance costo-beneficio.",
              "quality": 4 (número de 1 a 5 con la calidad estimada),
              "pros": "Excelente diseño, Confiable, Buen precio",
              "contras": "Envío costoso",
              "score": 85 (entero de 1 a 100 con la puntuación de compra prioritaria)
            }
        """.trimIndent()

        val request = MoshiGenerateContentRequest(
            contents = listOf(MoshiContent(parts = listOf(MoshiPart(text = prompt)))),
            generationConfig = MoshiGenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.3f
            )
        )

        try {
            val response = RetrofitClient.geminiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val cleanJson = cleanJsonResponse(jsonText)
                val adapter = RetrofitClient.moshiParser.adapter(ProductExtractionResult::class.java)
                val result = adapter.fromJson(cleanJson)
                if (result != null) {
                    return@withContext result
                }
            }
            throw Exception("No se pudo obtener una respuesta válida estructurada de Gemini")
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error calling Gemini API: ${e.localizedMessage}", e)
            val message = "Error de conexión o de API: ${e.localizedMessage}. Se ha completado de forma manual."
            return@withContext getLocalFallbackResult(url, message)
        }
    }

    /**
     * Sanitizes any extra characters around JSON response if present.
     */
    private fun cleanJsonResponse(response: String): String {
        var str = response.trim()
        if (str.startsWith("```json")) {
            str = str.substringAfter("```json")
        } else if (str.startsWith("```")) {
            str = str.substringAfter("```")
        }
        if (str.endsWith("```")) {
            str = str.substringBeforeLast("```")
        }
        return str.trim()
    }

    private fun getLocalFallbackResult(url: String, extraNotes: String): ProductExtractionResult {
        val domain = url.lowercase()
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .substringBefore("/")

        val storeName = when {
            domain.contains("amazon") -> "Amazon"
            domain.contains("mercadolibre") -> "Mercado Libre"
            domain.contains("aliexpress") -> "AliExpress"
            domain.contains("temu") -> "Temu"
            domain.contains("ebay") -> "eBay"
            else -> domain.capitalize()
        }

        val parsedName = url.substringAfterLast("/").substringBefore("?").replace("-", " ").replace("_", " ")
        val guessedTitle = if (parsedName.length > 5) parsedName.capitalize() else "Producto de $storeName"

        val defaultImg = "https://images.unsplash.com/photo-1523275335684-37898b6baf30" // Watch placeholder

        return ProductExtractionResult(
            title = guessedTitle,
            brand = "Marca de $storeName",
            price = 0.0,
            currency = "$",
            category = "Otros",
            description = "Enlace guardado manualmente. El análisis inteligente no pudo completarse de forma automática.",
            sourceStore = storeName,
            imageUrl = defaultImg,
            rating = 4.0f,
            summaryMd = """
                # Ficha de $guessedTitle
                
                ![Banner]($defaultImg)
                
                ## Datos Claves
                - **Enlace**: [$url]($url)
                - **Tienda**: $storeName
                - **Nota**: $extraNotes
                
                ## Resumen Ejecutivo
                El producto fue agregado usando detección manual rápida. Puedes editar libremente el precio, la categoría y los pros/contras en la pestaña de detalles para completar tu análisis de decisión.
                
                ## Pros y Contras
                ### Pros
                1. Almacenado localmente al instante.
                2. Historial de compras organizado por categoría.
                ### Contras
                1. Necesita completar detalles como precio o prioridad manualmente.
            """.trimIndent(),
            quality = 3,
            pros = "Fácil de guardar, Acceso rápido",
            contras = "Requiere entrada manual",
            score = 60
        )
    }
}
