package com.example.shiftplanet;

import static android.widget.Toast.LENGTH_SHORT;

import static com.example.shiftplanet.Registration.validPasswordCheck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shiftplanet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile extends AppCompatActivity {

    private EditText emailEditText, passwordEditText, fullNameEditText, phoneEditText;
    private Button emailUpdateBtn, passwordUpdateBtn, fullNameUpdateBtn, phoneUpdateBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        // אתחול הרכיבים מה-XML
        emailEditText = findViewById(R.id.updated_email);
        passwordEditText = findViewById(R.id.updated_password);
        fullNameEditText = findViewById(R.id.updated_full_name);
        phoneEditText = findViewById(R.id.updated_phone);

        emailUpdateBtn = findViewById(R.id.updated_email_btn);
        passwordUpdateBtn = findViewById(R.id.updated_password_btn);
        fullNameUpdateBtn = findViewById(R.id.updated_full_name_btn);
        phoneUpdateBtn = findViewById(R.id.updated_phone_btn);


        fullNameUpdateBtn.setOnClickListener(view -> {
            String newFullName = fullNameEditText.getText().toString().trim();
            if (newFullName.isEmpty()) {
                Toast.makeText(Profile.this, "enter updated full name", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("fullname", newFullName)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Full name successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "error updating full name", Toast.LENGTH_SHORT).show();
                    });
        });


        phoneUpdateBtn.setOnClickListener(view -> {
            String newPhone = phoneEditText.getText().toString().trim();
            if (newPhone.isEmpty()) {
                Toast.makeText(Profile.this, "enter updated phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId)
                    .update("phone", newPhone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "phone number successfully updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "error updating phone number", Toast.LENGTH_SHORT).show();
                    });
        });

        passwordUpdateBtn.setOnClickListener(view -> {
            String newPassword = passwordEditText.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(Profile.this, "enter updated password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validPasswordCheck(newPassword)){
                Toast.makeText(this, "password must contain 6 chars, at least 1 uppercase letter and at least 1 number ", LENGTH_SHORT).show();
                return;
            }

            // עדכון הסיסמה ב-Firebase Authentication
            mAuth.getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Profile.this, "password successfully updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Profile.this, "error updating password", Toast.LENGTH_SHORT).show();
                        }
                    });
        });



        emailUpdateBtn.setOnClickListener(view -> {
            String newEmail = emailEditText.getText().toString().trim();

            if (newEmail.isEmpty()) {
                Toast.makeText(Profile.this, "enter updated email", Toast.LENGTH_SHORT).show();
                return;
            }

            // עדכון האימייל ב-Firebase Authentication
            mAuth.getCurrentUser().updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // עדכון האימייל ב-Firestore
                            String userId = mAuth.getCurrentUser().getUid();
                            db.collection("users").document(userId)
                                    .update("email", newEmail)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(Profile.this, "email successfully updated", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(Profile.this, "error updating email", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(Profile.this, "error updating email in Firebase Authentication", Toast.LENGTH_SHORT).show();
                        }
                    });
        });






    }
}