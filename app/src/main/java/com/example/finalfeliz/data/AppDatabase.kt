package com.example.finalfeliz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finalfeliz.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Product::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finalfeliz.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Usa la instancia ya creada, sin llamar get(context) otra vez
                            val inst = INSTANCE ?: return
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // 1) Asegura Admin
                                    inst.userDao().ensureAdminExists(
                                        email = "admin@finalfeliz.cl",
                                        password = "Admin123."
                                    )
                                    // 2) Precarga productos si está vacío
                                    val pdao = inst.productDao()
                                    if (pdao.count() == 0) {
                                        pdao.insert(
                                            Product(
                                                name = "Clásico Nogal",
                                                material = "Madera maciza",
                                                priceClp = 500_000,
                                                imageRes = R.drawable.madera_maciza,
                                                description = "Modelo tradicional con acabado brillante."
                                            )
                                        )
                                        pdao.insert(
                                            Product(
                                                name = "Ébano Premium",
                                                material = "Acabado pulido",
                                                priceClp = 1_200_000,
                                                imageRes = R.drawable.pulido,
                                                description = "Ataúd elegante con detalles de lujo."
                                            )
                                        )
                                        pdao.insert(
                                            Product(
                                                name = "Serenidad Blanco",
                                                material = "Lacado mate",
                                                priceClp = 1_450_000,
                                                imageRes = R.drawable.ebano,
                                                description = "Diseño moderno con líneas suaves."
                                            )
                                        )
                                        pdao.insert(
                                            Product(
                                                name = "Roble Oscuro",
                                                material = "Textura natural",
                                                priceClp = 1_890_000,
                                                imageRes = R.drawable.natural,
                                                description = "Roble con vetas naturales."
                                            )
                                        )
                                        pdao.insert(
                                            Product(
                                                name = "Negro Granate",
                                                material = "Detalles metálicos",
                                                priceClp = 2_100_000,
                                                imageRes = R.drawable.premium,
                                                description = "Modelo exclusivo con acabados metálicos."
                                            )
                                        )
                                    }
                                } catch (_: Exception) { /* opcional log */ }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
