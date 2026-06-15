package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.repository.UserRepository
import com.example.open_sourcepart2.databinding.ActivityRegistrationBinding
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(name, email, password, confirmPassword)) {
                performRegistration(name, email, password)
            }
        }

        binding.tvAlreadyHaveAccount.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.tilName.error = "Name cannot be empty"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirm password cannot be empty"
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun performRegistration(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@RegistrationActivity)
                val repository = UserRepository(db.userDao())

                // Check if email already exists
                if (repository.isEmailRegistered(email)) {
                    Toast.makeText(this@RegistrationActivity, "Email already exists", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Register new user
                val userId = repository.registerUser(
                    name = name,
                    email = email,
                    age = 0,
                    password = password
                )

                if (userId > 0) {
                    Toast.makeText(this@RegistrationActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                    // Auto login after registration
                    val newUser = repository.login(email, password)
                    if (newUser != null) {
                        sessionManager.createLoginSession(newUser)
                        startActivity(Intent(this@RegistrationActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@RegistrationActivity, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}