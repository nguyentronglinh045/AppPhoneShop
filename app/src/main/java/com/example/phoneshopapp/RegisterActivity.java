package com.example.phoneshopapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.example.phoneshopapp.data.auth.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
  private static final String TAG = "RegisterActivity";

  private TextInputLayout fullNameLayout, emailLayout, phoneLayout, passwordLayout, confirmPasswordLayout;
  private MaterialButton registerButton;
  private LinearLayout googleButton, facebookButton;
  private TextView loginLink;
  private AuthRepository authRepository;
  private UserManager userManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Hide ActionBar
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    // Initialize dependencies
    authRepository = new AuthRepository();
    userManager = UserManager.getInstance(this);

    // Initialize views
    initializeViews();

    // Setup click listeners
    setupClickListeners();
  }

  private void initializeViews() {
    fullNameLayout = findViewById(R.id.fullNameLayout);
    emailLayout = findViewById(R.id.emailLayout);
    phoneLayout = findViewById(R.id.phoneLayout);
    passwordLayout = findViewById(R.id.passwordLayout);
    confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
    registerButton = findViewById(R.id.registerButton);
    googleButton = findViewById(R.id.googleButton);
    facebookButton = findViewById(R.id.facebookButton);
    loginLink = findViewById(R.id.loginLink);
  }

  private void setupClickListeners() {
    registerButton.setOnClickListener(v -> handleRegistration());

    googleButton.setOnClickListener(v -> {
      Toast.makeText(this, "Google registration clicked", Toast.LENGTH_SHORT).show();
    });

    facebookButton.setOnClickListener(v -> {
      Toast.makeText(this, "Facebook registration clicked", Toast.LENGTH_SHORT).show();
    });

    // Set click listener for login link
    loginLink.setOnClickListener(v -> {
      // Navigate back to login
      Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
      startActivity(intent);
      finish(); // Close the register activity
    });
  }

  private void handleRegistration() {
    if (!validateInputs()) {
      return;
    }

    String fullName = fullNameLayout.getEditText().getText().toString().trim();
    String email = emailLayout.getEditText().getText().toString().trim();
    String phone = phoneLayout.getEditText().getText().toString().trim();
    String password = passwordLayout.getEditText().getText().toString();

    String error = AuthRepository.validateEmailPassword(email, password);
    if (error != null) {
      Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
      return;
    }

    setLoading(true);
    Log.d(TAG, "Starting registration for: " + email);

    authRepository.registerWithEmail(email, password, fullName, phone)
        .addOnSuccessListener(result -> {
          FirebaseUser user = authRepository.getCurrentUser();

          if (user != null) {
            // Save complete user session
            String userId = user.getUid();
            String username = !fullName.isEmpty() ? fullName : email.split("@")[0];
            String displayName = !fullName.isEmpty() ? fullName : username;

            // Save user session with full information
            userManager.saveUserSession(userId, username, email, displayName, "user");
            userManager.setPhoneNumber(phone);

            // ...existing code...

            Log.d(TAG, "Registration successful, user saved: " + userManager.getUserInfoDebug());

            onRegistrationSuccess();
          } else {
            // Fallback - save basic info
            String username = !fullName.isEmpty() ? fullName : email.split("@")[0];
            userManager.saveUserInfo(username, email);
            userManager.setRole("user");
            userManager.setPhoneNumber(phone);

            onRegistrationSuccess();
          }
        })
        .addOnFailureListener(e -> {
          setLoading(false);
          Log.e(TAG, "Registration failed: " + e.getMessage());
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  private void onRegistrationSuccess() {
    setLoading(false);
    Toast.makeText(this, "Registration successful! Welcome to PhoneShop!", Toast.LENGTH_SHORT).show();

    // Navigate to main activity
    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  private boolean validateInputs() {
    boolean isValid = true;

    // Validate full name
    if (fullNameLayout.getEditText().getText().toString().trim().isEmpty()) {
      fullNameLayout.setError("Full name is required");
      isValid = false;
    } else {
      fullNameLayout.setError(null);
    }

    // Validate email
    String email = emailLayout.getEditText().getText().toString().trim();
    if (email.isEmpty()) {
      emailLayout.setError("Email is required");
      isValid = false;
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
      emailLayout.setError("Enter a valid email address");
      isValid = false;
    } else {
      emailLayout.setError(null);
    }

    // Validate phone
    String phone = phoneLayout.getEditText().getText().toString().trim();
    if (phone.isEmpty()) {
      phoneLayout.setError("Phone number is required");
      isValid = false;
    } else {
      phoneLayout.setError(null);
    }

    // Validate password
    String password = passwordLayout.getEditText().getText().toString();
    if (password.isEmpty()) {
      passwordLayout.setError("Password is required");
      isValid = false;
    } else if (password.length() < 6) {
      passwordLayout.setError("Password must be at least 6 characters");
      isValid = false;
    } else {
      passwordLayout.setError(null);
    }

    // Validate confirm password
    String confirmPassword = confirmPasswordLayout.getEditText().getText().toString();
    if (confirmPassword.isEmpty()) {
      confirmPasswordLayout.setError("Please confirm your password");
      isValid = false;
    } else if (!confirmPassword.equals(password)) {
      confirmPasswordLayout.setError("Passwords do not match");
      isValid = false;
    } else {
      confirmPasswordLayout.setError(null);
    }

    return isValid;
  }

  private void setLoading(boolean loading) {
    registerButton.setEnabled(!loading);
    registerButton.setText(loading ? "Creating account..." : "Register");
  }
}
