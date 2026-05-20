package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.ProductEntity
import java.io.File
import java.io.FileOutputStream

object MarkdownExporter {

    fun generateMarkdownContent(product: ProductEntity): String {
        return if (product.summaryMd.isNotBlank()) {
            product.summaryMd
        } else {
            """
            # ${product.title}
            
            ![Ficha](${product.imageUrl})
            
            ## Resumen Ejecutivo
            ${product.notes.ifBlank { "Deseo de compra analizado mediante inteligencia artificial." }}
            
            ## Información Clave
            - **Marca / Fabricante**: ${product.brand}
            - **Tienda**: ${product.sourceStore}
            - **Precio**: ${product.currency} ${product.price}
            - **Categoría**: ${product.category}
            - **Prioridad**: ${product.priority}
            - **Estado de Compra**: ${product.status}
            - **Puntuación de Compra**: ${product.score} / 100
            - **Calidad Estimada**: ${product.quality} / 5
            - **Enlace de Compra**: ${product.url}
            
            ## Pros y Contras
            ### Pros
            ${product.pros.split(",").joinToString("\n") { "- ${it.trim()}" }}
            
            ### Contras
            ${product.contras.split(",").joinToString("\n") { "- ${it.trim()}" }}
            
            ## Notas Personales
            ${product.notes.ifBlank { "Sin notas añadidas." }}
            
            ---
            *Ficha de decisión generada automáticamente en ShopWise el ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(product.createdAt))}*
            """.trimIndent()
        }
    }

    fun exportAndShare(context: Context, product: ProductEntity) {
        val mdContent = generateMarkdownContent(product)
        val sanitizedTitle = product.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
        val fileName = "ShopWise_${sanitizedTitle}.md"

        try {
            // Write to shared cache directory
            val cachePath = File(context.cacheDir, "markdown")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(mdContent.toByteArray())
            fileOutputStream.close()

            // Get URI using FileProvider
            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Ficha de Producto: ${product.title}")
                putExtra(Intent.EXTRA_TEXT, "Te comparto la ficha de decisión de compra de este producto guardado en ShopWise.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar Ficha (.md)"))
        } catch (e: Exception) {
            Log.e("MarkdownExporter", "Failed to export markdown file: ${e.message}", e)
            Toast.makeText(context, "Error al generar archivo .md", Toast.LENGTH_SHORT).show()
        }
    }
}
