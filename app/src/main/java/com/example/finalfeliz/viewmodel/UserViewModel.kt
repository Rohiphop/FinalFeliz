package com.example.finalfeliz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalfeliz.data.User
import com.example.finalfeliz.data.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// -----------------------------------------------------------
// Eventos one-shot
// -----------------------------------------------------------
sealed interface UserEvent {
    object LoginSuccess : UserEvent
    data class ShowMessage(val msg: String) : UserEvent

    // Perfil
    object ProfileNameSaved : UserEvent
    object PasswordChanged : UserEvent
    object PhoneSaved : UserEvent

    // Admin
    object UserUpdated : UserEvent
    object UserDeleted : UserEvent
}

// -----------------------------------------------------------
// Estado principal
// -----------------------------------------------------------
data class UserState(
    val loading: Boolean = false,
    val error: String? = null,

    val userName: String? = null,
    val userEmail: String? = null,
    val userPhone: String? = null,
    val isAdmin: Boolean = false,

    // Admin
    val users: List<User> = emptyList(),
    val userCount: Int = 0
)

// -----------------------------------------------------------
// ViewModel
// -----------------------------------------------------------
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    private val _events = MutableSharedFlow<UserEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UserEvent> = _events

    init {
        // Observa usuario actual
        viewModelScope.launch {
            repository.currentUserFlow.collectLatest { user ->
                _state.value = _state.value.copy(
                    userName = user?.name,
                    userEmail = user?.email,
                    userPhone = user?.phone,
                    isAdmin = (user?.isAdmin == true)
                )
            }
        }
    }

    // -----------------------------------------------------------
    // Registro
    // -----------------------------------------------------------
    fun register(name: String, email: String, password: String, phone: String? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val result = repository.register(name, email, password, phone)
            _state.value = if (result.isSuccess) {
                _state.value.copy(loading = false)
            } else {
                _state.value.copy(
                    loading = false,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // -----------------------------------------------------------
    // Login
    // -----------------------------------------------------------
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

    // -----------------------------------------------------------
    // Logout
    // -----------------------------------------------------------
    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = UserState()
        }
    }

    // ===========================================================
    // PERFIL
    // ===========================================================
    fun saveProfileName(newName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.updateCurrentUserName(newName)
            _state.value = if (r.isSuccess) {
                _state.value.copy(loading = false, error = null)
            } else {
                _state.value.copy(loading = false, error = r.exceptionOrNull()?.message)
            }
            if (r.isSuccess) _events.tryEmit(UserEvent.ProfileNameSaved)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.changeCurrentUserPassword(currentPassword, newPassword)
            _state.value = if (r.isSuccess) {
                _state.value.copy(loading = false, error = null)
            } else {
                _state.value.copy(loading = false, error = r.exceptionOrNull()?.message)
            }
            if (r.isSuccess) _events.tryEmit(UserEvent.PasswordChanged)
        }
    }

    fun savePhone(newPhone: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.updateCurrentUserPhone(newPhone)
            _state.value = if (r.isSuccess) {
                _state.value.copy(loading = false, error = null)
            } else {
                _state.value.copy(loading = false, error = r.exceptionOrNull()?.message)
            }
            if (r.isSuccess) _events.tryEmit(UserEvent.PhoneSaved)
        }
    }

    // ===========================================================
    // ADMIN
    // ===========================================================
    fun loadAdminData() {
        // Conservada por compatibilidad
        refreshAdmin()
    }

    fun refreshAdmin() {
        viewModelScope.launch {
            if (!_state.value.isAdmin) return@launch
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val list = repository.getAllUsers()
                val count = repository.getUserCount()
                _state.value = _state.value.copy(
                    loading = false,
                    users = list,
                    userCount = count,
                    error = null
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    fun setAdmin(userId: Long, admin: Boolean) {
        viewModelScope.launch {
            if (!_state.value.isAdmin) return@launch
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.setAdmin(userId, admin)
            if (r.isSuccess) {
                refreshAdmin()
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = r.exceptionOrNull()?.message
                )
            }
        }
    }

    // Editar un usuario completo (nombre, email, phone, isAdmin)
    fun update(user: User) {
        viewModelScope.launch {
            if (!_state.value.isAdmin) return@launch
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.updateUser(user)
            if (r.isSuccess) {
                _events.tryEmit(UserEvent.UserUpdated)
                refreshAdmin()
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = r.exceptionOrNull()?.message
                )
            }
        }
    }

    // Eliminar un usuario por id
    fun delete(id: Long) {
        viewModelScope.launch {
            if (!_state.value.isAdmin) return@launch
            _state.value = _state.value.copy(loading = true, error = null)
            val r = repository.deleteUser(id)
            if (r.isSuccess) {
                _events.tryEmit(UserEvent.UserDeleted)
                refreshAdmin()
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = r.exceptionOrNull()?.message
                )
            }
        }
    }
}
