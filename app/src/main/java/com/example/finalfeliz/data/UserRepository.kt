package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UserRepository(
    private val dao: UserDao,
    private val session: SessionManager
) {

    // ==============================
    // Helpers internos
    // ==============================
    private fun normalizeEmail(email: String): String =
        email.trim().lowercase()

    private fun cleanName(name: String): String =
        name.trim()

    // ==============================
    // Sesión observable
    // ==============================
    /** Flujo del usuario actual (null si no hay sesión). */
    val currentUserFlow: Flow<User?> =
        session.currentUserIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeUser(id)
        }

    /** Usuario actual una sola vez (no flow). */
    suspend fun getCurrentUserOnce(): User? {
        val id = session.getCurrentUserId() ?: return null
        return dao.findById(id)
    }

    // ==============================
    // Autenticación
    // ==============================
    suspend fun register(name: String, email: String, password: String): Result<Long> {
        val cleanEmail = normalizeEmail(email)
        val displayName = cleanName(name)

        // Validaciones mínimas (las fuertes ya las haces en UI)
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        }
        if (cleanEmail.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo no puede estar vacío"))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("La contraseña no puede estar vacía"))
        }

        val exists = dao.findByEmail(cleanEmail)
        if (exists != null) {
            return Result.failure(IllegalArgumentException("El email ya está registrado"))
        }

        // Usuario normal por defecto
        val id = dao.insert(
            User(
                name = displayName,
                email = cleanEmail,
                password = password,   // (demo) sin hash
                isAdmin = false
            )
        )

        session.setCurrentUserId(id)
        return Result.success(id)
    }

    suspend fun login(email: String, password: String): Result<User> {
        val cleanEmail = normalizeEmail(email)
        val user = dao.findByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        return if (user.password == password) {
            session.setCurrentUserId(user.id)
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña incorrectas"))
        }
    }

    suspend fun logout() {
        session.clearCurrentUserId()
    }

    // ==============================
    // Admin / Métricas
    // ==============================
    /** Cantidad total de usuarios. */
    suspend fun getUserCount(): Int = dao.getUserCount()

    /** Lista completa de usuarios (ordenados por ID desc). */
    suspend fun getAllUsers(): List<User> = dao.getAllUsers()

    /** Marca o desmarca a un usuario como admin. */
    suspend fun setAdmin(userId: Long, admin: Boolean) {
        dao.setAdmin(userId, admin)
    }

    /** Buscar un usuario por ID. */
    suspend fun findById(id: Long): User? = dao.findById(id)

    /** (Opcional) Garantiza que el admin exista. Útil para utilidades o tests. */
    suspend fun ensureDefaultAdmin(
        email: String = "admin@finalfeliz.cl",
        password: String = "Admin123."
    ) {
        dao.ensureAdminExists(normalizeEmail(email), password)
    }
}
