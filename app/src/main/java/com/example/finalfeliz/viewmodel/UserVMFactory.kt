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
        val db = AppDatabase.get(appContext)
        val repo = UserRepository(db.userDao(), SessionManager(appContext))
        return UserViewModel(repo) as T
    }
}
