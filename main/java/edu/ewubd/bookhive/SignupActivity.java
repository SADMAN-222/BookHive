package edu.ewubd.bookhive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends Activity {

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private CheckBox cbRememberUser, cbRememberLogin;
    private Button btnLogin, btnSignAdmin, btnSignUser;

    private FirebaseAuth mAuth;            // Firebase Authentication instance
    private FirebaseFirestore db;         // Firebase Firestore instance

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        String value = i.getStringExtra("abc");
        if ("f".equals(value)) {
            Toast.makeText(this, "Don't have an account", Toast.LENGTH_SHORT).show();
        }
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbRememberUser = findViewById(R.id.cbRememberUser);
        cbRememberLogin = findViewById(R.id.cbRememberLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignAdmin = findViewById(R.id.btnSignAdmin);
        btnSignUser = findViewById(R.id.btnSignUser);

        // Navigate to LoginActivity
        btnLogin.setOnClickListener(v -> {
            Intent i1 = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(i1);
            finishAffinity();
        });

        // Sign Up as a user
        btnSignUser.setOnClickListener(v -> processSignup(false));

        // Sign Up as an admin
        btnSignAdmin.setOnClickListener(v -> processSignup(true));
    }

    private void processSignup(boolean isAdmin) {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String conPass = etConfirmPassword.getText().toString().trim();

        // Validate input
        if (name.length() < 4) {
            showToast("User name should be 4-8 characters.");
            return;
        }
        if (!isValidEmail(email)) {
            showToast("Invalid email id.");
            return;
        }
        if (phone.length() < 8 || phone.length() > 13) {
            showToast("Phone number should be 8-13 digits.");
            return;
        }
        if (pass.length() < 6) {
            showToast("Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(conPass)) {
            showToast("Confirm password didn't match.");
            return;
        }

        // Firebase Authentication: Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user data to Firestore
                            String userId = user.getUid();
                            saveUserToFirestore(userId, name, email, phone, isAdmin, pass);  // Pass the password

                            // Redirect to Login page
                            goToLoginPage();
                        }
                    } else {
                        // If sign up fails, display a message to the user
                        showToast("Signup failed: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email, String phone, boolean isAdmin, String password) {
        // Create a user map to store in Firestore
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);       // Firebase Auth User ID
        userMap.put("name", name);           // Full Name
        userMap.put("email", email);         // Email Address
        userMap.put("phone", phone);         // Phone Number
        userMap.put("isAdmin", isAdmin);     // Admin/User flag
        userMap.put("rememberUser", cbRememberUser.isChecked()); // Remember User flag
        userMap.put("rememberLogin", cbRememberLogin.isChecked()); // Remember Login flag
        userMap.put("password", password);   // Store password (plain text, not recommended)
        userMap.put("timestamp", System.currentTimeMillis()); // Registration timestamp

        // Save user data in the "users" collection
        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> showToast("User details saved successfully."))
                .addOnFailureListener(e -> showToast("Failed to save user details: " + e.getMessage()));
    }

    // Go to login page
    private void goToLoginPage() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finishAffinity();
    }

    // Validate email using regular expression
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Show toast message
    private void showToast(String message) {
        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
