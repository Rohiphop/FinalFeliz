package com.example.finalfeliz.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalfeliz.data.AppDatabase
import com.example.finalfeliz.data.SessionManager
import com.example.finalfeliz.data.UserRepository

class UserVMFactory(private val appContext: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // 1. Obtener la instancia de la base de datos Room (singleton)
        val db = AppDatabase.get(appContext)

        // 2. Crear el repositorio de usuarios, pasando el DAO y el manejador de sesión
        val repo = UserRepository(
            dao = db.userDao(),
            session = SessionManager(appContext)
        )

        // 3. Retornar el ViewModel ya configurado con su repositorio
        return UserViewModel(repo) as T
    }
}
