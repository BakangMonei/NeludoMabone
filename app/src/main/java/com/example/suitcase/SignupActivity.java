package com.example.suitcase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Find the "Already have an account? Sign In" text view by its ID
        TextView signInLink = findViewById(R.id.textViewSignInLink);

        // Set an OnClickListener for the sign-in link
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the LoginActivity when the sign-in link is clicked
                openLoginActivity();
            }
        });

        // Find the "Sign Up" button by its ID
        Button signUpButton = findViewById(R.id.buttonSignUp);

        // Set an OnClickListener for the "Sign Up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call the method to sign up the user in the database
                signUpUser();
            }
        });

        // Add any other initialization code for the signup activity
        // ...
    }

    private void openLoginActivity() {
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Optional: finish the current activity
    }


    // Method to sign up the user in the database
    private void signUpUser() {
        // Find the email and password EditText views by their IDs
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextPassword = findViewById(R.id.editTextPassword);

        // Get the user's email and password from the EditText views
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate email and password
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Firebase Authentication to create a new user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User signed up successfully
                            Toast.makeText(SignupActivity.this, "User signed up!", Toast.LENGTH_SHORT).show();
                            Intent x = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(x);
                            // Open the LoginActivity after successful signup
//
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(SignupActivity.this, "Sign up failed. " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}