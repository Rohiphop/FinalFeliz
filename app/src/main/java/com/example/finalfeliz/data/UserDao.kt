package com.example.finalfeliz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    // Inserta un nuevo usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    // Inserta o reemplaza si el email ya existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    // Update completo por objeto (no siempre necesario para cambios simples)
    @Update
    suspend fun update(user: User)

    // ---------- Búsquedas ----------
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUser(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?

    // ---------- Lecturas para admin / métricas ----------
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<User>

    // ---------- Cambios de rol ----------
    @Query("UPDATE users SET isAdmin = :isAdmin WHERE id = :userId")
    suspend fun setAdmin(userId: Long, isAdmin: Boolean)

    // ---------- Ediciones de perfil ----------
    /** Actualiza solo el nombre del usuario. */
    @Query("UPDATE users SET name = :newName WHERE id = :userId")
    suspend fun updateName(userId: Long, newName: String)

    /** Cambia solo la contraseña del usuario. */
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    suspend fun updatePassword(userId: Long, newPassword: String)

    /**
     * Asegura que exista un usuario admin.
     * Si no existe, lo crea. Si existe, actualiza su contraseña y lo marca como admin.
     */
    @Transaction
    suspend fun ensureAdminExists(email: String, password: String) {
        val existing = findByEmail(email)
        if (existing == null) {
            insert(
                User(
                    name = "Administrador",
                    email = email,
                    password = password,            // plano (demo)
                    phone = "+56900000000",         // campo obligatorio agregado
                    isAdmin = true
                )
            )
        } else {
            if (!existing.isAdmin || existing.password != password) {
                update(existing.copy(isAdmin = true, password = password))
            }
        }
    }
}
