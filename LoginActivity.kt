package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.example.open_sourcepart2.database.AppDatabase
import com.example.open_sourcepart2.database.entities.User
import com.example.open_sourcepart2.database.repository.FinanceRepository
import com.example.open_sourcepart2.databinding.ActivityLoginBinding
import com.example.open_sourcepart2.managers.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Login Activity for AuriviaFinance application
 * Handles user authentication, validation, and session management
 * 
 * Features:
 * - Email/Password authentication
 * - Biometric/Fingerprint login
 * - Social login placeholders
 * - Session persistence
 * - Input validation
 */
class LoginActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "LoginActivity"
        private const val MAX_LOGIN_ATTEMPTS = 3
    }
    
    // View Binding
    private lateinit var binding: ActivityLoginBinding
    
    // Database and Repository
    private lateinit var database: AppDatabase
    private lateinit var repository: FinanceRepository
    private lateinit var sessionManager: SessionManager
    
    // Biometric Authentication
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricExecutor: ExecutorService
    
    // Login attempt counter for security
    private var loginAttempts = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize view binding
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Initialize database and session manager
            initializeDependencies()
            
            // Setup biometric authentication
            setupBiometricAuth()
            
            // Check if user is already logged in
            checkExistingSession()
            
            // Setup UI and click listeners
            setupClickListeners()
            
            // Setup auto-fill for development (remove in production)
            setupDevelopmentCredentials()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error initializing login screen", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    /**
     * Initializes all dependencies (Database, Repository, SessionManager)
     */
    private fun initializeDependencies() {
        try {
            database = AppDatabase.getDatabase(this)
            repository = FinanceRepository(database)
            sessionManager = SessionManager(this)
            Log.d(TAG, "Dependencies initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize dependencies: ${e.message}", e)
            throw RuntimeException("Failed to initialize app dependencies", e)
        }
    }
    
    /**
     * Sets up biometric authentication for fingerprint login
     */
    private fun setupBiometricAuth() {
        try {
            biometricExecutor = Executors.newSingleThreadExecutor()
            
            biometricPrompt = BiometricPrompt(
                this,
                biometricExecutor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Log.d(TAG, "Biometric authentication succeeded")
                        
                        // Try to login with saved credentials
                        CoroutineScope(Dispatchers.IO).launch {
                            loginWithSavedCredentials()
                        }
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.w(TAG, "Biometric authentication failed")
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "Fingerprint not recognized. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e(TAG, "Biometric authentication error: $errString")
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginActivity,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
            
            // Show biometric option if available
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Use fingerprint to access your account")
                .setNegativeButtonText("Use Password")
                .build()
            
            binding.btnBiometricLogin.setOnClickListener {
                try {
                    biometricPrompt.authenticate(promptInfo)
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing biometric prompt: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Biometric authentication not available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup biometric auth: ${e.message}", e)
            binding.btnBiometricLogin.visibility = android.view.View.GONE
        }
    }
    
    /**
     * Checks if user has saved session and redirects to main activity
     */
    private fun checkExistingSession() {
        try {
            if (sessionManager.isLoggedIn()) {
                Log.d(TAG, "User already logged in, redirecting to MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing session: ${e.message}", e)
        }
    }
    
    /**
     * Sets up all click listeners for the activity
     */
    private fun setupClickListeners() {
        try {
            // Login button click handler
            binding.btnLogin.setOnClickListener {
                try {
                    val email = binding.etEmail.text.toString().trim()
                    val password = binding.etPassword.text.toString().trim()
                    
                    if (validateInputs(email, password)) {
                        performLogin(email, password)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in login click: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Error processing login",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Sign up navigation
            binding.tvSignUp.setOnClickListener {
                try {
                    startActivity(Intent(this, RegistrationActivity::class.java))
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to registration: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Unable to open registration",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Forgot password
            binding.tvForgotPassword?.setOnClickListener {
                try {
                    showForgotPasswordDialog()
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing forgot password dialog: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Password reset feature coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            // Social login buttons (placeholders for future implementation)
            setupSocialLoginListeners()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}", e)
        }
    }
    
    /**
     * Sets up social login button listeners (placeholders)
     */
    private fun setupSocialLoginListeners() {
        try {
            binding.ivGoogle.setOnClickListener {
                Toast.makeText(
                    this,
                    "Google login coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Implement Google Sign-In
                // authenticateWithGoogle()
            }
            
            binding.ivGithub.setOnClickListener {
                Toast.makeText(
                    this,
                    "GitHub login coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Implement GitHub OAuth
            }
            
            binding.ivFacebook.setOnClickListener {
                Toast.makeText(
                    this,
                    "Facebook login coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Implement Facebook Login
            }
            
            binding.ivInstagram.setOnClickListener {
                Toast.makeText(
                    this,
                    "Instagram login coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
                // TODO: Implement Instagram OAuth
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up social login listeners: ${e.message}", e)
        }
    }
    
    /**
     * Performs login with email and password using Room database
     * 
     * @param email User's email address
     * @param password User's password
     */
    private fun performLogin(email: String, password: String) {
        // Disable login button to prevent multiple attempts
        binding.btnLogin.isEnabled = false
        
        // Show loading indicator
        showLoading(true)
        
        // Perform login in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Validate against database using Room
                val user = repository.loginUser(email, password)
                
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // Login successful
                        handleSuccessfulLogin(user)
                    } else {
                        // Login failed
                        handleFailedLogin()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleLoginError(e)
                }
            } finally {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }
    
    /**
     * Handles successful login by saving session and navigating to main activity
     * 
     * @param user The authenticated user object
     */
    private fun handleSuccessfulLogin(user: User) {
        try {
            Log.d(TAG, "User logged in successfully: ${user.email}")
            
            // Save user session
            sessionManager.saveUserSession(user)
            
            // Reset login attempts counter
            loginAttempts = 0
            
            // Show success message
            Toast.makeText(
                this,
                "Welcome back, ${user.name}!",
                Toast.LENGTH_SHORT
            ).show()
            
            // Navigate to Main Activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling successful login: ${e.message}", e)
            Toast.makeText(
                this,
                "Error saving login session",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * Handles failed login attempts with security measures
     */
    private fun handleFailedLogin() {
        try {
            loginAttempts++
            
            val remainingAttempts = MAX_LOGIN_ATTEMPTS - loginAttempts
            val message = if (remainingAttempts > 0) {
                "Invalid email or password. $remainingAttempts attempts remaining."
            } else {
                "Too many failed attempts. Please try again later."
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            
            // Disable login after max attempts
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                binding.btnLogin.isEnabled = false
                Toast.makeText(
                    this,
                    "Login temporarily disabled. Please try again in 5 minutes.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Re-enable after 5 minutes (300000 milliseconds)
                binding.btnLogin.postDelayed({
                    binding.btnLogin.isEnabled = true
                    loginAttempts = 0
                }, 300000)
            }
            
            // Clear password field
            binding.etPassword.text?.clear()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling failed login: ${e.message}", e)
        }
    }
    
    /**
     * Handles login errors with appropriate user feedback
     */
    private fun handleLoginError(e: Exception) {
        try {
            val errorMessage = when {
                e.message?.contains("database", ignoreCase = true) == true -> 
                    "Database error. Please try again."
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Network error. Check your connection."
                else -> 
                    "Login failed. Please try again."
            }
            
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            Log.e(TAG, "Login error details: ${e.message}", e)
            
        } catch (toastError: Exception) {
            Log.e(TAG, "Error showing login error message: ${toastError.message}")
        }
    }
    
    /**
     * Attempts to login using saved credentials (for biometric authentication)
     */
    private suspend fun loginWithSavedCredentials() {
        try {
            val savedEmail = sessionManager.getSavedEmail()
            val savedPassword = sessionManager.getSavedPassword()
            
            if (savedEmail != null && savedPassword != null) {
                val user = repository.loginUser(savedEmail, savedPassword)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        handleSuccessfulLogin(user)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Saved credentials invalid. Please login manually.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging with saved credentials: ${e.message}", e)
        }
    }
    
    /**
     * Validates user input for email and password
     * 
     * @param email User's email address
     * @param password User's password
     * @return true if inputs are valid, false otherwise
     */
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        try {
            // Email validation
            if (email.isEmpty()) {
                binding.tilEmail.error = "Email cannot be empty"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Please enter a valid email address"
                isValid = false
            } else {
                binding.tilEmail.error = null
            }
            
            // Password validation
            if (password.isEmpty()) {
                binding.tilPassword.error = "Password cannot be empty"
                isValid = false
            } else if (password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                binding.tilPassword.error = null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating inputs: ${e.message}", e)
            isValid = false
        }
        
        return isValid
    }
    
    /**
     * Shows loading progress bar while authenticating
     * 
     * @param isLoading True to show loading, false to hide
     */
    private fun showLoading(isLoading: Boolean) {
        try {
            binding.progressBar?.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnLogin.visibility = if (isLoading) android.view.View.GONE else android.view.View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading state: ${e.message}", e)
        }
    }
    
    /**
     * Shows forgot password dialog
     */
    private fun showForgotPasswordDialog() {
        try {
            val dialog = android.app.AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("Enter your email address to receive password reset instructions.")
                .setView(android.widget.EditText(this).apply {
                    hint = "Email address"
                    inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                })
                .setPositiveButton("Send") { _, input ->
                    val email = (input as? android.widget.EditText)?.text.toString().trim()
                    if (email.isNotEmpty()) {
                        sendPasswordResetEmail(email)
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
            
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing forgot password dialog: ${e.message}", e)
        }
    }
    
    /**
     * Sends password reset email (to be implemented with Firebase or backend)
     */
    private fun sendPasswordResetEmail(email: String) {
        try {
            // TODO: Implement password reset functionality with backend service
            Toast.makeText(
                this,
                "Password reset instructions sent to $email",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending password reset: ${e.message}", e)
        }
    }
    
    /**
     * Sets up development credentials for testing (remove in production)
     */
    private fun setupDevelopmentCredentials() {
        // Only for development - remove in production
        if (BuildConfig.DEBUG) {
            binding.etEmail.setText("test@example.com")
            binding.etPassword.setText("password123")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            biometricExecutor.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down biometric executor: ${e.message}", e)
        }
    }
}
