package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.repository.UserRepository
import com.example.open_sourcepart2.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

// LoginActivity.kt
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        // Social login buttons (keep as is)
        binding.ivGoogle.setOnClickListener {
            Toast.makeText(this, "Google login not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.ivGithub.setOnClickListener {
            Toast.makeText(this, "GitHub login not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.ivFacebook.setOnClickListener {
            Toast.makeText(this, "Facebook login not implemented", Toast.LENGTH_SHORT).show()
        }

        binding.ivInstagram.setOnClickListener {
            Toast.makeText(this, "Instagram login not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@LoginActivity)
                val repository = UserRepository(db.userDao())

                val user = repository.login(email, password)

                if (user != null) {
                    sessionManager.createLoginSession(user)
                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}