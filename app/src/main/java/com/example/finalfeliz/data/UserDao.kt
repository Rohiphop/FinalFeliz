package com.example.finalfeliz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    // Upsert para casos donde quieres reemplazar (por email único)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUser(id: Long): Flow<User?>

    // Admin helpers
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users ORDER BY id DESC")
    suspend fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?


    // Asegura que el admin exista (insert si no existe / update si cambió algo)
    @Transaction
    suspend fun ensureAdminExists(email: String, password: String) {
        val found = findByEmail(email)
        if (found == null) {
            insert(
                User(
                    name = "Administrador",
                    email = email,
                    password = password,
                    isAdmin = true
                )
            )
        } else {
            // si existe, nos aseguramos que sea admin y con la password conocida
            if (!found.isAdmin || found.password != password) {
                update(found.copy(isAdmin = true, password = password))
            }
        }
    }

    @Query("UPDATE users SET isAdmin = :isAdmin WHERE id = :userId")
    suspend fun setAdmin(userId: Long, isAdmin: Boolean)
}
