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

    @Update
    suspend fun update(user: User)

    // Busca por email (para login o seed admin)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    // Busca por ID como Flow (para observar cambios)
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUser(id: Long): Flow<User?>

    // Retorna cantidad total
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // Lista todos los usuarios (por ejemplo para admin)
    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<User>

    // Busca puntual por ID
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?

    // Marca o desmarca un usuario como administrador
    @Query("UPDATE users SET isAdmin = :isAdmin WHERE id = :userId")
    suspend fun setAdmin(userId: Long, isAdmin: Boolean)

    /**
     * ✅ Asegura que exista un usuario admin
     * Si no existe, lo crea.
     * Si existe, actualiza su contraseña y flag de admin.
     */
    @Transaction
    suspend fun ensureAdminExists(email: String, password: String) {
        val existing = findByEmail(email)
        if (existing == null) {
            insert(
                User(
                    name = "Administrador",
                    email = email,
                    password = password, // plano (demo)
                    isAdmin = true
                )
            )
        } else {
            // Si ya existe, asegura que esté marcado como admin y tenga la contraseña correcta
            if (!existing.isAdmin || existing.password != password) {
                update(existing.copy(isAdmin = true, password = password))
            }
        }
    }
}
