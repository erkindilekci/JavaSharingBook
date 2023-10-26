package com.erkindilekci.javasharingbook.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.erkindilekci.javasharingbook.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private FirebaseAuth mAuth;

    String email;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btSignIn.setOnClickListener(v -> handleSignInButtonCLick());

        binding.btSignUp.setOnClickListener(v -> handleSignUpButtonCLick());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Toast.makeText(getApplicationContext(), "Logged into: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, FeedActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private void handleSignInButtonCLick() {
        email = binding.etEmail.getText().toString().strip();
        password = binding.etPassword.getText().toString().strip();

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Field(s) can't be empty.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "You've successfully signed in.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, FeedActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            if (task.getException().getMessage().contains("INVALID_LOGIN_CREDENTIALS")) {
                                Toast.makeText(this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void handleSignUpButtonCLick() {
        email = binding.etEmail.getText().toString().strip();
        password = binding.etPassword.getText().toString().strip();

        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Field(s) can't be empty.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "You've successfully signed up.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(this, FeedActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}