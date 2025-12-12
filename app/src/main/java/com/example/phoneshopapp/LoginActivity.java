package com.example.phoneshopapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.example.phoneshopapp.data.auth.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
  private static final String TAG = "LoginActivity";
  private static final String PREF_RESET_COUNT = "reset_password_count";
  private static final String PREF_RESET_TIME = "reset_password_time";
  private static final int MAX_RESET_ATTEMPTS = 5;
  private static final long RESET_WINDOW_MS = 60 * 60 * 1000; // 1 hour

  private TextInputLayout emailLayout, passwordLayout;
  private TextView forgotPassword;
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
    forgotPassword = findViewById(R.id.forgotPassword);
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

    // Xử lý khi bấm "Quên mật khẩu"
    forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
  }

  private void showForgotPasswordDialog() {
    // Check rate limit
    SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
    int resetCount = prefs.getInt(PREF_RESET_COUNT, 0);
    long lastResetTime = prefs.getLong(PREF_RESET_TIME, 0);
    long currentTime = System.currentTimeMillis();

    // Reset counter if window has passed
    if (currentTime - lastResetTime > RESET_WINDOW_MS) {
      resetCount = 0;
      prefs.edit().putInt(PREF_RESET_COUNT, 0).apply();
    }

    if (resetCount >= MAX_RESET_ATTEMPTS) {
      long remainingTime = RESET_WINDOW_MS - (currentTime - lastResetTime);
      int remainingMinutes = (int) (remainingTime / 60000);
      Toast.makeText(this, 
          "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau " + remainingMinutes + " phút.", 
          Toast.LENGTH_LONG).show();
      return;
    }

    View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
    TextInputLayout resetEmailLayout = dialogView.findViewById(R.id.resetEmailLayout);
    TextInputEditText resetEmailInput = dialogView.findViewById(R.id.resetEmailInput);
    ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
    TextView textRateLimit = dialogView.findViewById(R.id.textRateLimit);

    // Pre-fill email từ login form nếu có
    String currentEmail = emailEditText.getText().toString().trim();
    if (!currentEmail.isEmpty()) {
      resetEmailInput.setText(currentEmail);
    }

    // Hiển thị số lần còn lại
    int remaining = MAX_RESET_ATTEMPTS - resetCount;
    textRateLimit.setText("Còn " + remaining + " lần gửi trong giờ này");
    textRateLimit.setVisibility(View.VISIBLE);

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(dialogView)
        .setPositiveButton("Gửi", null) // Set null để custom behavior
        .setNegativeButton("Hủy", null)
        .create();

    dialog.setOnShowListener(dialogInterface -> {
      Button sendButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
      sendButton.setOnClickListener(v -> {
        String email = resetEmailInput.getText().toString().trim();
        
        // Validate email
        if (email.isEmpty()) {
          resetEmailLayout.setError("Vui lòng nhập email");
          return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
          resetEmailLayout.setError("Email không hợp lệ");
          return;
        }
        resetEmailLayout.setError(null);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

        // Send reset email
        authRepository.sendPasswordReset(email)
            .addOnSuccessListener(unused -> {
              // Update rate limit counter
              SharedPreferences.Editor editor = prefs.edit();
              editor.putInt(PREF_RESET_COUNT, prefs.getInt(PREF_RESET_COUNT, 0) + 1);
              editor.putLong(PREF_RESET_TIME, System.currentTimeMillis());
              editor.apply();

              progressBar.setVisibility(View.GONE);
              dialog.dismiss();
              Toast.makeText(LoginActivity.this, 
                  "Email đặt lại mật khẩu đã được gửi đến " + email, 
                  Toast.LENGTH_LONG).show();
            })
            .addOnFailureListener(e -> {
              progressBar.setVisibility(View.GONE);
              sendButton.setEnabled(true);
              dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
              
              String errorMessage = e.getMessage();
              if (errorMessage != null && errorMessage.contains("no user record")) {
                resetEmailLayout.setError("Email không tồn tại trong hệ thống");
              } else {
                Toast.makeText(LoginActivity.this, 
                    "Lỗi: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
              }
            });
      });
    });

    dialog.show();
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
