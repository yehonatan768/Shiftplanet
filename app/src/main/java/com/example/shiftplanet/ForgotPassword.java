package com.example.shiftplanet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    private ImageView backButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.forgotPasswordEmail);
        resetPasswordButton = findViewById(R.id.btnResetPassword);
        backButton = findViewById(R.id.btnBackForgotPassword);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                if (email.isEmpty()) {
                    emailEditText.setError("Please enter your email");
                    return;
                }

                sendPasswordResetEmail(email);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this,
                                "Check your email to reset your password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ForgotPassword.this,
                                "Failed to send reset email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void back() {
        Intent intent = new Intent(ForgotPassword.this, Login.class);
        startActivity(intent);
        finish();
    }
}
