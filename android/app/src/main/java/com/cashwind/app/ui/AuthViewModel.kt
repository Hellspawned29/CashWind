package com.cashwind.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cashwind.app.model.User
import com.cashwind.app.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    
    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun register(email: String, password: String, name: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.register(email, password, name)
                _user.postValue(response.user)
                _error.postValue(null)
            } catch (t: Throwable) {
                _error.postValue(t.message ?: "Registration failed")
                _user.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun login(email: String, password: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.login(email, password)
                _user.postValue(response.user)
                _error.postValue(null)
            } catch (t: Throwable) {
                _error.postValue(t.message ?: "Login failed")
                _user.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun logout() {
        repo.logout()
        _user.value = null
    }

    fun isLoggedIn(): Boolean = repo.isLoggedIn()
}
