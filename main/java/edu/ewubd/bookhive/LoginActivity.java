package edu.ewubd.bookhive;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private CheckBox cbRememberUser, cbRememberLogin;
    private Button btnSignup, btnLoginAdmin, btnLoginUser;

    private FirebaseAuth mAuth; // Firebase Authentication instance
    private FirebaseFirestore db; // Firebase Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberUser = findViewById(R.id.cbRememberUser);
        cbRememberLogin = findViewById(R.id.cbRememberLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnLoginAdmin = findViewById(R.id.btnLoginAdmin);
        btnLoginUser = findViewById(R.id.btnLoginUser);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is already logged in and populate fields
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            populateFieldsFromFirestore(currentUser.getUid());
        }

        // Login as Admin
        btnLoginAdmin.setOnClickListener(v -> loginUserOrAdmin(true));

        // Login as User
        btnLoginUser.setOnClickListener(v -> loginUserOrAdmin(false));

        // Navigate to Signup Activity
        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finishAffinity();
        });
    }

    private void populateFieldsFromFirestore(String userId) {
        // Fetch user data from Firestore
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String email = document.getString("email");

                        // Populate fields with data from Firestore
                        if (email != null) {
                            etEmail.setText(email);
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUserOrAdmin(boolean isAdmin) {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Email or Password cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if the email is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        // Authenticate with Firebase
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Check user role and navigate accordingly
                            checkUserRole(user.getUid(), isAdmin);
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        handleLoginError(error);
                    }
                });
    }

    private void checkUserRole(String userId, boolean isAdminLogin) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        boolean isUserAdmin = document.getBoolean("isAdmin") != null && document.getBoolean("isAdmin");

                        if (isAdminLogin && !isUserAdmin) {
                            Toast.makeText(LoginActivity.this, "You can't login as an admin.", Toast.LENGTH_LONG).show();
                        } else if (!isAdminLogin && isUserAdmin) {
                            Toast.makeText(LoginActivity.this, "You can't login as a user.", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, BooklistActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        }
                    } else {
                        Toast.makeText(this, "Failed to determine user role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleLoginError(String error) {
        if (error.contains("The password is invalid")) {
            Toast.makeText(LoginActivity.this, "Invalid password. Please try again.", Toast.LENGTH_LONG).show();
        } else if (error.contains("There is no user record corresponding to this identifier")) {
            Toast.makeText(LoginActivity.this, "User does not exist. Please sign up.", Toast.LENGTH_LONG).show();
        } else if (error.contains("The email address is badly formatted")) {
            Toast.makeText(LoginActivity.this, "The email address is invalid. Please check the format.", Toast.LENGTH_LONG).show();
        } else if (error.contains("Network error")) {
            Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
        }
    }
}
