package com.example.shiftplanet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var userType: String? = null
    private var mAuth: FirebaseAuth? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        try {
            initializeUI()
            initializeFirebase()
            setButtonListeners()
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization: " + e.message)
            showToast("An unexpected error occurred. Please restart the app.")
        }
    }

    private fun initializeUI() {
        emailEditText = findViewById(R.id.inputEmailLogin)
        passwordEditText = findViewById(R.id.inputPasswordLogin)
    }

    private fun initializeFirebase() {
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun setButtonListeners() {
        findViewById<View>(R.id.btnLogin).setOnClickListener { view: View? ->
            try {
                loginUser()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error during login: " + e.message
                )
                showToast("An error occurred while logging in. Please try again.")
            }
        }
        findViewById<View>(R.id.forgotPasswordLogin).setOnClickListener { view: View? ->
            navigateTo(
                ForgotPassword::class.java
            )
        }
        findViewById<View>(R.id.createNewAccount).setOnClickListener { view: View? ->
            navigateTo(
                Registration::class.java
            )
        }
    }

    fun loginUser() {
        val email = emailEditText!!.text.toString().trim { it <= ' ' }
        val password = passwordEditText!!.text.toString().trim { it <= ' ' }

        if (isInputValid(email, password)) {
            signInUser(email, password)
        } else {
            showToast("Please enter both email and password.")
        }
    }

    private fun isInputValid(email: String, password: String): Boolean {
        return !email.isEmpty() && !password.isEmpty()
    }

    private fun signInUser(email: String, password: String) {
        Log.d(
            TAG,
            "Attempting to sign in user: $email"
        )
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    val user = mAuth!!.currentUser
                    if (user != null) {
                        if (!user.isEmailVerified) {
                            showToast("Please verify your email before logging in.")
                            mAuth!!.signOut()
                            return@addOnCompleteListener
                        }
                        fetchUserData(user.uid, email)
                    }
                } else {
                    val errorMessage = if (task.exception != null)
                        task.exception!!.message
                    else
                        "Unknown error"
                    Log.e(
                        TAG,
                        "Authentication error: $errorMessage"
                    )
                    showToast("Authentication failed. Please check your credentials.")
                }
            }
    }

    private fun fetchUserData(userId: String, email: String) {
        Log.d(
            TAG,
            "Fetching user data for UID: $userId"
        )
        db!!.collection("users")
            .document(userId)
            .get()
            .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        userType =
                            document.getString("userType") // Ensure field name matches Firestore
                        if (userType != null) {
                            navigateToHomePage(email)
                        } else {
                            Log.e(
                                TAG,
                                "User type is missing."
                            )
                            showToast("User type is missing in Firestore. Please contact support.")
                        }
                    } else {
                        Log.e(
                            TAG,
                            "User document does not exist."
                        )
                        showToast("User document does not exist.")
                    }
                } else {
                    val errorMessage = if (task.exception != null)
                        task.exception!!.message
                    else
                        "Unknown error"
                    Log.e(
                        TAG,
                        "Error retrieving user data: $errorMessage"
                    )
                    showToast("Failed to retrieve user data from Firestore.")
                }
            }
    }

    private fun navigateToHomePage(email: String) {
        val intent: Intent
        try {
            intent = if ("Manager".equals(userType, ignoreCase = true)) {
                Intent(this@Login, ManagerHomePage::class.java)
            } else if ("Employee".equals(userType, ignoreCase = true)) {
                Intent(this@Login, EmployeeHomePage::class.java)
            } else {
                throw IllegalArgumentException("Unknown user type: $userType")
            }

            // Attach the email as an extra
            intent.putExtra("LOGIN_EMAIL", email)

            Log.d(
                TAG,
                "Navigating to: $userType Home Page with email: $email"
            )
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            showToast("Error navigating to home page. Please contact support.")
        }
    }

    private fun navigateTo(targetActivity: Class<*>) {
        val intent = Intent(this@Login, targetActivity)
        startActivity(intent)
    }


    private fun showToast(message: String) {
        Toast.makeText(this@Login, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}