package com.example.finalfeliz.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestiona la sesión actual del usuario dentro de la aplicación.
 * Guarda el ID del usuario autenticado en SharedPreferences
 * y lo expone mediante un Flow para que otros componentes
 * (por ejemplo, ViewModels) reaccionen a los cambios de sesión.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("finalfeliz_session", Context.MODE_PRIVATE)

    // Flujo observable del ID del usuario actual
    private val _currentUserIdFlow =
        MutableStateFlow<Long?>(prefs.getLong("user_id", -1).takeIf { it != -1L })
    val currentUserIdFlow = _currentUserIdFlow.asStateFlow()


     //Guarda el ID del usuario actual y actualiza el flujo en memoria.

    fun setCurrentUserId(id: Long) {
        prefs.edit().putLong("user_id", id).apply()
        _currentUserIdFlow.value = id
    }


     //Elimina el ID del usuario actual (logout).

    fun clearCurrentUserId() {
        prefs.edit().remove("user_id").apply()
        _currentUserIdFlow.value = null
    }


     //Devuelve el ID actual del usuario, o null si no hay sesión activa.

    fun getCurrentUserId(): Long? {
        val id = prefs.getLong("user_id", -1)
        return if (id == -1L) null else id
    }
}
