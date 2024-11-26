package com.example.upnext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Activity_SignUp extends BaseActivity {
    private EditText emailEditText, passwordEditText;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Bind views
        this.emailEditText = findViewById(R.id.emailEditText);
        this.passwordEditText = findViewById(R.id.passwordEditText);
        this.signUpButton = findViewById(R.id.signupButton);
        signUpButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(Activity_SignUp.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            signUpUser(email, password);
        });
    }

    private void signUpUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        saveUserInfo(uid,email);
                        // Navigate to MainActivity on successful sign-up
                        Toast.makeText(Activity_SignUp.this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Activity_SignUp.this, MainActivity.class));
                        finish();
                    } else {
                        // Show error message
                        Toast.makeText(Activity_SignUp.this, "Sign-Up Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }



    public void switchToLogin(View view) {
        android.util.Log.d("Activity_SignUp", "Switch to Login button clicked");
        Intent i = new Intent(Activity_SignUp.this, Activity_Login.class);
        startActivity(i);

        Log.d("Activity_SignUp", "Intent started");
        finish();
    }
}