package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UserRepository(
    private val dao: UserDao,
    private val session: SessionManager
) {
    // Flujo del usuario actual (null si no hay sesión)
    val currentUserFlow: Flow<User?> =
        session.currentUserIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeUser(id)
        }

    // -------------------- Auth básica --------------------

    suspend fun register(name: String, email: String, password: String): Result<Long> {
        val exists = dao.findByEmail(email)
        if (exists != null) {
            return Result.failure(IllegalArgumentException("El email ya está registrado"))
        }
        // Por defecto: usuario normal (isAdmin = false)
        val id = dao.insert(
            User(
                name = name,
                email = email,
                password = password,
                isAdmin = false
            )
        )
        // guarda sesión
        session.setCurrentUserId(id)
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val user = dao.findByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        return if (user.password == password) {
            // guarda sesión
            session.setCurrentUserId(user.id)
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectas"))
        }
    }

    suspend fun logout() {
        session.clearCurrentUserId()
    }

    // -------------------- Admin / Métricas --------------------

    /** Cantidad total de usuarios en la tabla. */
    suspend fun getUserCount(): Int = dao.getUserCount()

    /** Lista completa de usuarios (ordenada por id DESC). */
    suspend fun getAllUsers(): List<User> = dao.getAllUsers()

    /** Marca o desmarca a un usuario como admin. */
    suspend fun setAdmin(userId: Long, admin: Boolean) = dao.setAdmin(userId, admin)

    /** (Conveniencia) Buscar por id puntual. */
    suspend fun findById(id: Long): User? = dao.findById(id)
}
