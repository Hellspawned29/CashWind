package com.cashwind.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.cashwind.app.databinding.ActivityLoginBinding
import com.cashwind.app.ui.AuthViewModel
import com.cashwind.app.repository.AuthRepository
import com.cashwind.app.util.TokenManager
import com.cashwind.app.network.RetrofitProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authRepository by lazy { AuthRepository(TokenManager(this)) }
    private val viewModel: AuthViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository) as T
            }
        }
    }

    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Retrofit with context
        RetrofitProvider.init(this)

        // Check if already logged in
        if (viewModel.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        binding.loginButton.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        binding.toggleButton.setOnClickListener {
            toggleMode()
        }

        // Observe ViewModel state
        viewModel.user.observe(this) { user ->
            if (user != null) {
                navigateToDashboard()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.loginButton.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { error ->
            if (error != null) {
                binding.errorText.text = error
                binding.errorText.visibility = android.view.View.VISIBLE
            } else {
                binding.errorText.visibility = android.view.View.GONE
            }
        }
    }

    private fun performLogin() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            binding.errorText.text = "Email and password required"
            binding.errorText.visibility = android.view.View.VISIBLE
            return
        }

        viewModel.login(email, password)
    }

    private fun performRegister() {
        val email = binding.emailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val name = binding.nameInput.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            binding.errorText.text = "All fields required"
            binding.errorText.visibility = android.view.View.VISIBLE
            return
        }

        viewModel.register(email, password, name)
    }

    private fun toggleMode() {
        isLoginMode = !isLoginMode
        if (isLoginMode) {
            binding.loginButton.text = "Login"
            binding.toggleButton.text = "Need an account? Register"
            binding.nameInput.visibility = android.view.View.GONE
            binding.title.text = "Cashwind Login"
        } else {
            binding.loginButton.text = "Register"
            binding.toggleButton.text = "Already have an account? Login"
            binding.nameInput.visibility = android.view.View.VISIBLE
            binding.title.text = "Cashwind Register"
        }
        binding.errorText.visibility = android.view.View.GONE
    }

    private fun navigateToDashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
