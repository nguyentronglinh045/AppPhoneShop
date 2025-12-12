package com.example.phoneshopapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.example.phoneshopapp.data.auth.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
  private static final String TAG = "LoginActivity";

  private TextInputLayout emailLayout, passwordLayout;
  private TextInputEditText emailEditText, passwordEditText;
  private Button loginButton;
  private TextView registerLink;
  // ...existing code...
  private LinearLayout googleButton, facebookButton;
  private AuthRepository authRepository;
  private UserManager userManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Ẩn ActionBar
    if (getSupportActionBar() != null) {
      getSupportActionBar().hide();
    }

    // Khởi tạo dependencies
    authRepository = new AuthRepository();
    userManager = UserManager.getInstance(this);

    // Khởi tạo view
    initializeViews();

    // ...existing code...

    // Setup click listeners
    setupClickListeners();
  }

  private void initializeViews() {
    emailLayout = findViewById(R.id.emailLayout);
    passwordLayout = findViewById(R.id.passwordLayout);
    emailEditText = (TextInputEditText) emailLayout.getEditText();
    passwordEditText = (TextInputEditText) passwordLayout.getEditText();
    loginButton = findViewById(R.id.loginButton);
    registerLink = findViewById(R.id.registerLink);
    // ...existing code...
    googleButton = findViewById(R.id.googleButton);
    facebookButton = findViewById(R.id.facebookButton);
  }

  private void loadSavedCredentials() {
    // ...existing code...
  }

  private void setupClickListeners() {

    // Xử lý khi bấm nút Login
    loginButton.setOnClickListener(v -> handleLogin());

    // Xử lý khi bấm nút Google
    googleButton.setOnClickListener(v -> {
      // Toast.makeText(this, "Đăng nhập Google được nhấn", Toast.LENGTH_SHORT).show();
    });

    // Xử lý khi bấm nút Facebook
    facebookButton.setOnClickListener(v -> {
      // Toast.makeText(this, "Đăng nhập Facebook được nhấn", Toast.LENGTH_SHORT).show();
    });

    // Chuyển sang màn hình Register
    registerLink.setOnClickListener(v -> {
      Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
      startActivity(intent);
    });
  }

  private void handleLogin() {
    String email = emailEditText.getText().toString().trim();
    String password = passwordEditText.getText().toString().trim();
    // ...existing code...

    String error = AuthRepository.validateEmailPassword(email, password);
    if (error != null) {
      Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
      return;
    }

    setLoading(true);

    authRepository.loginWithEmail(email, password)
        .addOnSuccessListener(result -> {
          FirebaseUser user = authRepository.getCurrentUser();
          String username = (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty())
              ? user.getDisplayName()
              : email.split("@")[0];

          // ...existing code...

          // Save user session with full info
          if (user != null) {
            userManager.saveUserSession(
                user.getUid(),
                username,
                email,
                user.getDisplayName() != null ? user.getDisplayName() : username,
                "user" // Default role, will be updated from Firestore
            );

            // Fetch role from Firestore
            authRepository.getUserRole(user.getUid())
                .addOnSuccessListener(role -> {
                  userManager.setRole(role);
                  onLoginSuccess();
                })
                .addOnFailureListener(e -> {
                  // If role fetch fails, proceed with default user role
                  userManager.setRole("user");
                  onLoginSuccess();
                });
          } else {
            // Shouldn't happen, but proceed safely
            userManager.saveUserInfo(username, email);
            userManager.setRole("user");
            onLoginSuccess();
          }
        })
        .addOnFailureListener(e -> {
          setLoading(false);
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        });
  }

  private void onLoginSuccess() {
    setLoading(false);
    Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

    Log.d(TAG, "Login successful: " + userManager.getUserInfoDebug());

    startActivity(new Intent(LoginActivity.this, MainActivity.class));
    finish();
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Chỉ kiểm tra nếu user đã đăng nhập thì chuyển vào MainActivity
    if (authRepository == null) {
      authRepository = new AuthRepository();
    }
    FirebaseUser current = authRepository.getCurrentUser();
    if (current != null && userManager.isLoggedIn()) {
      Log.d(TAG, "User already logged in, navigating to main");
      startActivity(new Intent(LoginActivity.this, MainActivity.class));
      finish();
    }
  }

  private void navigateToMain() {
    startActivity(new Intent(LoginActivity.this, MainActivity.class));
    finish();
  }

  private void setLoading(boolean loading) {
    loginButton.setEnabled(!loading);
    loginButton.setText(loading ? "Đang đăng nhập..." : "ĐĂNG NHẬP");
  }
}
