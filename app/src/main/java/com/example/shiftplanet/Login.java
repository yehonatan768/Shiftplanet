package com.example.shiftplanet;



import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText emailEditText, passwordEditText;
    private String userType;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this is correctly set in strings.xml
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        setContentView(R.layout.login);

        try {
            initializeUI();
            initializeFirebase();
            setButtonListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error during initialization: " + e.getMessage());
            showToast("An unexpected error occurred. Please restart the app.");
        }
    }

    private void initializeUI() {
        emailEditText = findViewById(R.id.inputEmailLogin);
        passwordEditText = findViewById(R.id.inputPasswordLogin);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setButtonListeners() {
        findViewById(R.id.btnLogin).setOnClickListener(view -> {
            try {
                loginUser();
            } catch (Exception e) {
                Log.e(TAG, "Error during login: " + e.getMessage());
                showToast("An error occurred while logging in. Please try again.");
            }
        });

        findViewById(R.id.btnGoogle).setOnClickListener(view -> {
            try {
                loginUsingGoogle();
            } catch (Exception e) {
                Log.e(TAG, "Error during login with google: " + e.getMessage());
                showToast("An error occurred while logging in with google account. Please try again.");
            }
        });

        findViewById(R.id.forgotPasswordLogin).setOnClickListener(view -> navigateTo(ForgotPassword.class));
        findViewById(R.id.createNewAccount).setOnClickListener(view -> navigateTo(Registration.class));
    }

    public void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (isInputValid(email, password)) {
            signInUser(email, password);
        } else {
            showToast("Please enter both email and password.");
        }
    }

    public void loginUsingGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Log.w(TAG, "Google sign-in failed", e);
                showToast("Google sign-in failed. Please try again.");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserInFirestore(user);  // Check Firestore before proceeding
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            Log.w(TAG, "Sign-in with Google failed", exception);
                            handleFirebaseAuthError(exception, acct.getEmail());
                        } else {
                            showToast("Authentication failed.");
                        }
                    }
                });
    }


    private void handleFirebaseAuthError(Exception exception, String email) {
        if (exception.getMessage() != null && exception.getMessage().contains("The email address is already in use")) {
            showToast("This email is already registered. Please sign in using Email & Password.");

            // Redirect the user to the login screen with the email filled in
            Intent intent = new Intent(Login.this, Login.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
        } else {
            showToast("Sign-in failed: " + exception.getMessage());
        }
    }



    private void checkUserInFirestore(FirebaseUser user) {
        String uid = user.getUid();
        String name = user.getDisplayName();
        String email = user.getEmail();
        String phone = user.getPhoneNumber(); // Might be null

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User exists, get their role and navigate accordingly
                        Log.d(TAG, "User exists in Firestore. Logging in...");
                        userType = documentSnapshot.getString("userType");

                        if (userType != null) {
                            navigateToHomePage(email);
                        } else {
                            showToast("Error: User type not found. Please contact support.");
                        }
                    } else {
                        // User does not exist, navigate to CompleteRegistration
                        Log.d(TAG, "New Google user. Redirecting to Complete Registration...");
                        Intent intent = new Intent(Login.this, CompleteRegistration.class);
                        intent.putExtra("FULL_NAME", name);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("PHONE", phone);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore check failed", e);
                    showToast("Error checking user data. Try again.");
                });
    }



    private boolean isInputValid(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    private void signInUser(String email, String password) {
        Log.d(TAG, "Attempting to sign in user: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            if (!user.isEmailVerified()) {
                                showToast("Please verify your email before logging in.");
                                mAuth.signOut();
                                return;
                            }
                            fetchUserData(user.getUid(), email);
                        }
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Log.e(TAG, "Authentication error: " + errorMessage);
                        showToast("Authentication failed. Please check your credentials.");
                    }
                });
    }

    private void fetchUserData(String userId, String email) {
        Log.d(TAG, "Fetching user data for UID: " + userId);
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            userType = document.getString("userType"); // Ensure field name matches Firestore
                            if (userType != null) {
                                navigateToHomePage(email);
                            } else {
                                Log.e(TAG, "User type is missing.");
                                showToast("User type is missing in Firestore. Please contact support.");
                            }
                        } else {
                            Log.e(TAG, "User document does not exist.");
                            showToast("User document does not exist.");
                        }
                    } else {
                        String errorMessage = task.getException() != null
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Log.e(TAG, "Error retrieving user data: " + errorMessage);
                        showToast("Failed to retrieve user data from Firestore.");
                    }
                });
    }

    private void navigateToHomePage(String email) {
        Intent intent;
        try {
            if ("Manager".equalsIgnoreCase(userType)) {
                intent = new Intent(Login.this, ManagerHomePage.class);
            } else if ("Employee".equalsIgnoreCase(userType)) {
                intent = new Intent(Login.this, EmployeeHomePage.class);
            } else {
                throw new IllegalArgumentException("Unknown user type: " + userType);
            }

            // Attach the email as an extra
            intent.putExtra("LOGIN_EMAIL", email);

            Log.d(TAG, "Navigating to: " + userType + " Home Page with email: " + email);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            showToast("Error navigating to home page. Please contact support.");
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Login.this, targetActivity);
        startActivity(intent);
    }


    private void showToast(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
    }
}
