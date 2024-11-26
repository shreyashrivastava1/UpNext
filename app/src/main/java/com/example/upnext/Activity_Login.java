package com.example.upnext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_Login extends BaseActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Activity_Login", "Login Activity started");
        super.onCreate(savedInstanceState);

        // Enable Edge-to-Edge UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Adjust system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Activity_Login.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });
    }

    private void loginUser(String email, String password) {
        loginButton.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Navigate to MainActivity on successful login
                        Toast.makeText(Activity_Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Activity_Login.this, MainActivity.class));
                        finish();
                    } else {
                        // Show error message
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
                        Toast.makeText(Activity_Login.this, "Login Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        switchToSignUp();
                    }
                });
    }

    public void switchToSignUp() {
        new AlertDialog.Builder(this)
                .setTitle("Login Failed")
                .setMessage("Would you like to create a new account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent i = new Intent(Activity_Login.this, Activity_SignUp.class);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();

    }
    public void switchToSignUp(View v){
        Intent i = new Intent(Activity_Login.this, Activity_SignUp.class);
        startActivity(i);
        finish();

    }
}
