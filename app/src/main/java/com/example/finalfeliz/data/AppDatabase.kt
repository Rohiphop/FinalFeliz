package com.example.finalfeliz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class],
    version = 3,               // <-- súbelo si vienes de otra versión
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finalfeliz.db"
                )
                    .fallbackToDestructiveMigration() // evita error de integridad al cambiar schema
                    .addCallback(seedAdminCallback(context))
                    .build()
                    .also { INSTANCE = it }
            }

        private fun seedAdminCallback(context: Context) =
            object : Callback() {

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Garantiza que el admin exista SIEMPRE que abras la app.
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val dao = get(context).userDao()
                            dao.ensureAdminExists(
                                email = "admin@finalfeliz.cl",
                                password = "Admin123."
                            )
                        } catch (_: Exception) { /* log si deseas */ }
                    }
                }
            }
    }
}
