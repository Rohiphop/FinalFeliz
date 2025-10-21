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

// ----------- Eventos one-shot para la UI -----------
sealed interface UserEvent {
    object LoginSuccess : UserEvent
    data class ShowMessage(val msg: String) : UserEvent
}

data class UserState(
    val loading: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    // --- Admin ---
    val isAdmin: Boolean = false,
    val users: List<User> = emptyList(),
    val userCount: Int = 0
)

class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    // Bus de eventos (one-shot)
    private val _events = MutableSharedFlow<UserEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UserEvent> = _events

    init {
        // Observa al usuario actual (se actualiza con login/register/logout)
        viewModelScope.launch {
            repository.currentUserFlow.collectLatest { u ->
                _state.value = _state.value.copy(
                    userName = u?.name,
                    userEmail = u?.email,
                    isAdmin = (u?.isAdmin == true)
                )
            }
        }
    }

    // -------------------- Auth --------------------

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = repository.register(name, email, password)
            _state.value = if (result.isSuccess) {
                _state.value.copy(loading = false) // datos llegarán por currentUserFlow
            } else {
                _state.value.copy(loading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = repository.login(email, password)
            if (result.isSuccess) {
                _state.value = _state.value.copy(loading = false, error = null)
                _events.tryEmit(UserEvent.LoginSuccess)
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "Correo o contraseña incorrectas"
                )
                _events.tryEmit(UserEvent.ShowMessage("Correo o contraseña incorrectas"))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()                 // limpia sesión
            _state.value = UserState()          // limpia estado UI
        }
    }

    // -------------------- Admin --------------------

    /** Carga el conteo y el listado de usuarios si el actual es admin. */
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

    /** Asigna o quita admin a un usuario. Refresca la lista luego. */
    fun setAdmin(userId: Long, admin: Boolean) {
        viewModelScope.launch {
            if (_state.value.isAdmin) {
                repository.setAdmin(userId, admin)
                loadAdminData()
            }
        }
    }
}
