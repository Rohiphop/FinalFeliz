package com.example.finalfeliz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.finalfeliz.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Product::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN phone TEXT NOT NULL DEFAULT '+56900000000'")
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finalfeliz.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // rendimiento
                    .addCallback(object : Callback() {

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            val inst = INSTANCE ?: return
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // Asegura Admin una vez creada la DB
                                    inst.userDao().ensureAdminExists(
                                        email = "admin@finalfeliz.cl",
                                        password = "Admin123."
                                    )

                                    // Seed de productos en batch
                                    val pdao = inst.productDao()
                                    pdao.insertAll(
                                        listOf(
                                            Product(
                                                name = "Clásico Nogal",
                                                material = "Madera maciza",
                                                priceClp = 500_000,
                                                imageRes = R.drawable.madera_maciza,
                                                description = "Modelo tradicional con acabado brillante."
                                            ),
                                            Product(
                                                name = "Ébano Premium",
                                                material = "Acabado pulido",
                                                priceClp = 1_200_000,
                                                imageRes = R.drawable.pulido,
                                                description = "Ataúd elegante con detalles de lujo."
                                            ),
                                            Product(
                                                name = "Serenidad Blanco",
                                                material = "Lacado mate",
                                                priceClp = 1_450_000,
                                                imageRes = R.drawable.ebano,
                                                description = "Diseño moderno con líneas suaves."
                                            ),
                                            Product(
                                                name = "Roble Oscuro",
                                                material = "Textura natural",
                                                priceClp = 1_890_000,
                                                imageRes = R.drawable.natural,
                                                description = "Roble con vetas naturales."
                                            ),
                                            Product(
                                                name = "Negro Granate",
                                                material = "Detalles metálicos",
                                                priceClp = 2_100_000,
                                                imageRes = R.drawable.premium,
                                                description = "Modelo exclusivo con acabados metálicos."
                                            )
                                        )
                                    )
                                } catch (_: Exception) {}
                            }
                        }

                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
