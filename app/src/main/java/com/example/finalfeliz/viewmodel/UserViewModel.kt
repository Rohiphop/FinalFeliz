package com.example.finalfeliz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalfeliz.data.User
import com.example.finalfeliz.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

// -----------------------------------------------------------
// Eventos one-shot: mensajes y resultados únicos hacia la UI
// -----------------------------------------------------------
sealed interface UserEvent {
    object LoginSuccess : UserEvent
    data class ShowMessage(val msg: String) : UserEvent
}

// -----------------------------------------------------------
// Estado principal que refleja la información del usuario
// -----------------------------------------------------------
data class UserState(
    val loading: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val isAdmin: Boolean = false,          // indica si el usuario tiene privilegios
    val users: List<User> = emptyList(),   // lista visible solo para admins
    val userCount: Int = 0                 // conteo total de usuarios
)

// -----------------------------------------------------------
// ViewModel que maneja autenticación y administración de usuarios
// -----------------------------------------------------------
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    // Estado expuesto a la UI
    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    // Canal para enviar eventos de una sola vez (no persistentes)
    private val _events = MutableSharedFlow<UserEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UserEvent> = _events

    init {
        // Observa al usuario actual de forma reactiva (Room + Flow)
        // Esto actualiza la UI automáticamente cuando cambia el usuario
        viewModelScope.launch {
            repository.currentUserFlow.collectLatest { user ->
                _state.value = _state.value.copy(
                    userName = user?.name,
                    userEmail = user?.email,
                    isAdmin = (user?.isAdmin == true)
                )
            }
        }
    }

    // -----------------------------------------------------------
    // Registro de nuevos usuarios
    // -----------------------------------------------------------
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = repository.register(name, email, password)

            _state.value = if (result.isSuccess) {
                // Si el registro fue exitoso, la sesión se actualiza por currentUserFlow
                _state.value.copy(loading = false)
            } else {
                // Si ocurrió un error, lo reflejamos en el estado
                _state.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // -----------------------------------------------------------
    // Inicio de sesión
    // -----------------------------------------------------------
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = repository.login(email, password)

            if (result.isSuccess) {
                // Éxito: actualiza el estado y emite evento a la UI
                _state.value = _state.value.copy(loading = false, error = null)
                _events.tryEmit(UserEvent.LoginSuccess)
            } else {
                // Falla: muestra mensaje de error
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Correo o contraseña incorrectas"
                )
                _events.tryEmit(UserEvent.ShowMessage("Correo o contraseña incorrectas"))
            }
        }
    }

    // -----------------------------------------------------------
    // Cierre de sesión
    // -----------------------------------------------------------
    fun logout() {
        viewModelScope.launch {
            repository.logout()                // limpia la sesión actual
            _state.value = UserState()         // reinicia el estado en la UI
        }
    }

    // -----------------------------------------------------------
    // Funcionalidades exclusivas para administradores
    // -----------------------------------------------------------

    /** Carga el conteo y la lista de usuarios, solo si el actual es administrador. */
    fun loadAdminData() {
        viewModelScope.launch {
            if (_state.value.isAdmin) {
                val list = repository.getAllUsers()
                val count = repository.getUserCount()
                _state.value = _state.value.copy(
                    users = list,
                    userCount = count
                )
            }
        }
    }

    /** Cambia el rol de un usuario (asignar o quitar privilegios de admin). */
    fun setAdmin(userId: Long, admin: Boolean) {
        viewModelScope.launch {
            if (_state.value.isAdmin) {
                repository.setAdmin(userId, admin)
                loadAdminData() // refresca la lista tras el cambio
            }
        }
    }
}
