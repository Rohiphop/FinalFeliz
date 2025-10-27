package com.example.finalfeliz.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UserRepository(
    private val dao: UserDao,
    private val session: SessionManager
) {

    // Default para cumplir contrato no-nulo del teléfono
    private companion object {
        private const val DEFAULT_PHONE = "+56900000000"
    }

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
    val currentUserFlow: Flow<User?> =
        session.currentUserIdFlow.flatMapLatest { id ->
            if (id == null) flowOf(null) else dao.observeUser(id)
        }

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
        val cleanEmail   = normalizeEmail(email)
        val displayName  = cleanName(name)
        val normalizedPh = phone?.let(::cleanPhone)?.ifBlank { DEFAULT_PHONE } ?: DEFAULT_PHONE

        if (displayName.isBlank()) return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        if (cleanEmail.isBlank())  return Result.failure(IllegalArgumentException("El correo no puede estar vacío"))
        if (password.isBlank())    return Result.failure(IllegalArgumentException("La contraseña no puede estar vacía"))

        val exists = dao.findByEmail(cleanEmail)
        if (exists != null) return Result.failure(IllegalArgumentException("El email ya está registrado"))

        val id = dao.insert(
            User(
                name = displayName,
                email = cleanEmail,
                password = password,   // (demo sin hash)
                isAdmin = false,
                phone = normalizedPh
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
    suspend fun updateCurrentUserName(newName: String): Result<Unit> = runCatching {
        val id = currentUserIdOrFail()
        val clean = cleanName(newName)
        require(clean.isNotBlank()) { "El nombre no puede estar vacío" }
        dao.updateName(id, clean)
    }

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
        dao.updatePassword(id, newPassword)
    }

    suspend fun updateCurrentUserPhone(newPhone: String): Result<Unit> = runCatching {
        val id = currentUserIdOrFail()
        val normalized = cleanPhone(newPhone).ifBlank { DEFAULT_PHONE }
        require(normalized.length >= 8) { "Ingresa un teléfono válido" }
        val user = dao.findById(id) ?: error("Usuario no encontrado")
        dao.update(user.copy(phone = normalized))
    }

    // ==============================
    // Admin / Métricas
    // ==============================
    suspend fun getUserCount(): Int = dao.getUserCount()

    suspend fun getAllUsers(): List<User> = dao.getAllUsers()

    suspend fun setAdmin(userId: Long, admin: Boolean): Result<Unit> = runCatching {
        dao.setAdmin(userId, admin)
    }

    suspend fun findById(id: Long): User? = dao.findById(id)

    suspend fun ensureDefaultAdmin(
        email: String = "admin@finalfeliz.cl",
        password: String = "Admin123."
    ) {
        dao.ensureAdminExists(normalizeEmail(email), password)
    }

    // ==============================
    // Admin: CRUD
    // ==============================
    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        val cleanEmail  = normalizeEmail(user.email)
        val cleanName   = cleanName(user.name)
        val cleanPhone  = cleanPhone(user.phone).ifBlank { DEFAULT_PHONE }

        require(cleanName.isNotBlank())  { "El nombre no puede estar vacío" }
        require(cleanEmail.isNotBlank()) { "El correo no puede estar vacío" }

        // Evitar duplicado de email con otro usuario
        val existing = dao.findByEmail(cleanEmail)
        if (existing != null && existing.id != user.id) {
            throw IllegalArgumentException("Ya existe un usuario con este correo")
        }

        dao.update(
            user.copy(
                name  = cleanName,
                email = cleanEmail,
                phone = cleanPhone
            )
        )
    }

    suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        val user = dao.findById(id) ?: throw IllegalArgumentException("Usuario no encontrado")
        dao.delete(user)
    }
}
