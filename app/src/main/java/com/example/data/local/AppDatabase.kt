package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ProductEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "carrito_universal_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Migración de esquema v1 a v2:
                // Las tablas existentes se mantienen, solo se agregan nuevas columnas
                database.execSQL("ALTER TABLE products ADD COLUMN priceAlertEnabled INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE products ADD COLUMN priceAlertThreshold REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE products ADD COLUMN priceHistory TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
