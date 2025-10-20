package com.example.finalfeliz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalfeliz.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


data class UserState(
    val loading: Boolean = false,
    val error: String? = null,
    val userName: String? = null,
    val userEmail: String? = null
)


class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state

    init {
        // Observa al usuario actual directo desde Room (vía DataStore -> userId)
        viewModelScope.launch {
            repository.currentUserFlow.collectLatest { u ->
                _state.value = _state.value.copy(
                    userName = u?.name,
                    userEmail = u?.email
                )
            }
        }
    }

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
            _state.value = if (result.isSuccess) {
                _state.value.copy(loading = false) // datos llegarán por currentUserFlow
            } else {
                _state.value.copy(loading = false, error = "Correo o contraseña incorrectas")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()     // limpia userId en DataStore
            _state.value = UserState() // limpia estado UI
        }
    }
}
