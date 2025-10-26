package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UserRepository(
    private val dao: UserDao,
    private val session: SessionManager
) {

    // ==============================
    // Normalizadores / helpers
    // ==============================
    private fun normalizeEmail(email: String): String =
        email.trim().lowercase()

    private fun cleanName(name: String): String =
        name.trim()

    private fun cleanPhone(phone: String): String =
        phone.trim().replace(" ", "")

    private suspend fun currentUserIdOrFail(): Long =
        session.getCurrentUserId()
            ?: throw IllegalStateException("No hay sesión activa")

    // ==============================
    // Sesión observable
    // ==============================
    /** Flujo del usuario actual (null si no hay sesión). */
    val currentUserFlow: Flow<User?> =
        session.currentUserIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeUser(id)
        }

    /** Usuario actual una sola vez. */
    suspend fun getCurrentUserOnce(): User? {
        val id = session.getCurrentUserId() ?: return null
        return dao.findById(id)
    }

    // ==============================
    // Autenticación
    // ==============================
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String? = null
    ): Result<Long> {
        val cleanEmail = normalizeEmail(email)
        val displayName = cleanName(name)
        val cleanPhone = phone?.let(::cleanPhone)

        if (displayName.isBlank()) return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        if (cleanEmail.isBlank())  return Result.failure(IllegalArgumentException("El correo no puede estar vacío"))
        if (password.isBlank())    return Result.failure(IllegalArgumentException("La contraseña no puede estar vacía"))

        val exists = dao.findByEmail(cleanEmail)
        if (exists != null) return Result.failure(IllegalArgumentException("El email ya está registrado"))

        val id = dao.insert(
            User(
                name = displayName,
                email = cleanEmail,
                password = password, // demo: sin hash
                isAdmin = false,
                phone = cleanPhone
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
    // Perfil del usuario actual
    // ==============================

    /** Actualiza solo el nombre del usuario actual. */
    suspend fun updateCurrentUserName(newName: String): Result<Unit> = runCatching {
        val id = currentUserIdOrFail()
        val clean = cleanName(newName)
        require(clean.isNotBlank()) { "El nombre no puede estar vacío" }
        dao.updateName(id, clean)
    }

    /**
     * Cambia la contraseña del usuario actual.
     * Valida la contraseña actual antes de aplicar el cambio.
     */
    suspend fun changeCurrentUserPassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val id = currentUserIdOrFail()
        val user = dao.findById(id) ?: error("Usuario no encontrado")
        require(currentPassword.isNotBlank()) { "Debes ingresar tu contraseña actual" }
        require(newPassword.isNotBlank()) { "La nueva contraseña no puede estar vacía" }

        if (user.password != currentPassword) {
            throw IllegalArgumentException("La contraseña actual no coincide")
        }
        // Si más adelante aplicas hashing, haz la verificación y guardado con hash aquí.
        dao.updatePassword(id, newPassword)
    }

    /** Actualiza el teléfono del usuario actual. Guarda siempre el número limpio (sin espacios). */
    suspend fun updateCurrentUserPhone(newPhone: String): Result<Unit> = runCatching {
        val id = currentUserIdOrFail()
        val normalized = cleanPhone(newPhone)
        // Validación mínima; ajusta a tu formato esperado
        require(normalized.length >= 8) { "Ingresa un teléfono válido" }
        val user = dao.findById(id) ?: error("Usuario no encontrado")
        dao.update(user.copy(phone = normalized))
    }

    // ==============================
    // Admin / Métricas
    // ==============================
    suspend fun getUserCount(): Int = dao.getUserCount()

    suspend fun getAllUsers(): List<User> = dao.getAllUsers()

    suspend fun setAdmin(userId: Long, admin: Boolean) {
        dao.setAdmin(userId, admin)
    }

    suspend fun findById(id: Long): User? = dao.findById(id)

    /** Garantiza que el admin por defecto exista. */
    suspend fun ensureDefaultAdmin(
        email: String = "admin@finalfeliz.cl",
        password: String = "Admin123."
    ) {
        dao.ensureAdminExists(normalizeEmail(email), password)
    }
}
