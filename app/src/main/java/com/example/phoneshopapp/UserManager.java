package com.example.phoneshopapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.phoneshopapp.utils.PreferencesManager;

/**
 * UserManager - Quản lý thông tin user đăng nhập
 * Tích hợp với PreferencesManager để quản lý dữ liệu persistent
 */
public class UserManager {
  private static UserManager instance;
  private static final String TAG = "UserManager";

  // Backward compatibility
  private static final String PREF_NAME = "user_prefs";
  private static final String KEY_USERNAME = "username";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_IS_LOGGED_IN = "is_logged_in";
  private static final String KEY_ROLE = "role"; // "user" | "admin"

  private SharedPreferences prefs;
  private PreferencesManager preferencesManager;

  private UserManager(Context context) {
    prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    preferencesManager = PreferencesManager.getInstance(context);

    // Migrate old data to new PreferencesManager if needed
    migrateOldDataIfNeeded();
  }

  public static UserManager getInstance(Context context) {
    if (instance == null) {
      instance = new UserManager(context.getApplicationContext());
    }
    return instance;
  }

  /**
   * Migrate dữ liệu từ SharedPreferences cũ sang PreferencesManager mới
   */
  private void migrateOldDataIfNeeded() {
    if (prefs.contains(KEY_USERNAME) && !preferencesManager.isUserLoggedIn()) {
      String oldUsername = prefs.getString(KEY_USERNAME, "");
      String oldEmail = prefs.getString(KEY_EMAIL, "");
      String oldRole = prefs.getString(KEY_ROLE, "user");
      boolean wasLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

      if (wasLoggedIn && !oldUsername.isEmpty()) {
        Log.d(TAG, "Migrating old user data to PreferencesManager");
        preferencesManager.saveUserSession(
            getCurrentUserId() != null ? getCurrentUserId() : "migrated_user",
            oldUsername,
            oldEmail,
            oldUsername, // Use username as display name
            oldRole);

        // Clear old data after migration
        prefs.edit().clear().apply();
      }
    }
  }

  // === MAIN USER METHODS (using PreferencesManager) ===

  /**
   * Lưu thông tin user khi login thành công (enhanced)
   */
  public void saveUserInfo(String username, String email) {
    Log.d(TAG, "Saving user info: " + username + " (" + email + ")");
    preferencesManager.saveBasicUserInfo(username, email);
  }

  /**
   * Lưu thông tin user đầy đủ sau login/register thành công
   */
  public void saveUserSession(String userId, String username, String email, String displayName, String role) {
    Log.d(TAG, "Saving full user session");
    preferencesManager.saveUserSession(userId, username, email, displayName, role);
  }

  /**
   * Lưu thông tin user từ Firebase Auth
   */
  public void saveUserFromFirebase(FirebaseUser firebaseUser) {
    if (firebaseUser != null) {
      String userId = firebaseUser.getUid();
      String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
      String displayName = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "";
      String username = !displayName.isEmpty() ? displayName : email.split("@")[0];

      saveUserSession(userId, username, email, displayName, "user");
      Log.d(TAG, "User saved from Firebase: " + username);
    }
  }

  // Lấy tên user
  public String getUsername() {
    String username = preferencesManager.getUsername();
    return !username.isEmpty() ? username : "Guest User";
  }

  // Lấy email user
  public String getUserEmail() {
    String email = preferencesManager.getUserEmail();
    return !email.isEmpty() ? email : "guest@phoneshop.com";
  }

  // Kiểm tra user đã login chưa
  public boolean isLoggedIn() {
    return preferencesManager.isUserLoggedIn() && getCurrentUserId() != null;
  }

  // Lấy Firebase User ID (UID)
  public String getCurrentUserId() {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser != null) {
      return currentUser.getUid();
    }

    // Fallback to PreferencesManager if Firebase user not available
    String savedUserId = preferencesManager.getUserId();
    return !savedUserId.isEmpty() ? savedUserId : null;
  }

  // Role management (using PreferencesManager)
  public void setRole(String role) {
    preferencesManager.setUserRole(role);
  }

  public String getRole() {
    return preferencesManager.getUserRole();
  }

  // === ENHANCED USER INFO METHODS ===

  /**
   * Lấy display name từ PreferencesManager
   */
  public String getDisplayName() {
    String displayName = preferencesManager.getDisplayName();

    if (!displayName.isEmpty()) {
      return displayName;
    }

    // Fallback logic
    String username = getUsername();
    switch (username.toLowerCase()) {
      case "admin":
        return "Administrator";
      case "truc":
        return "Truc Nguyen";
      case "user":
        return "User";
      default:
        if (username.length() > 0) {
          return username.substring(0, 1).toUpperCase() + username.substring(1);
        }
        return username;
    }
  }

  /**
   * Cập nhật display name
   */
  public void setDisplayName(String displayName) {
    preferencesManager.setDisplayName(displayName);
  }

  /**
   * Lấy phone number
   */
  public String getPhoneNumber() {
    return preferencesManager.getPhoneNumber();
  }

  /**
   * Set phone number
   */
  public void setPhoneNumber(String phoneNumber) {
    preferencesManager.setPhoneNumber(phoneNumber);
  }

  /**
   * Lấy profile image URL
   */
  public String getProfileImageUrl() {
    return preferencesManager.getProfileImageUrl();
  }

  /**
   * Set profile image URL
   */
  public void setProfileImageUrl(String imageUrl) {
    preferencesManager.setProfileImageUrl(imageUrl);
  }

  // Lấy email hiển thị dựa trên username
  public String getDisplayEmail() {
    String email = getUserEmail();

    // Nếu email đã được set, dùng email đó
    if (!email.equals("guest@phoneshop.com")) {
      return email;
    }

    // Nếu không, tạo email dựa trên username
    String username = getUsername();
    return username.toLowerCase() + "@phoneshop.com";
  }

  // ...existing code...

  // Xóa thông tin user khi logout
  public void logout() {
    Log.d(TAG, "User logging out");

    // Sign out from Firebase
    FirebaseAuth.getInstance().signOut();

    // Clear user session from PreferencesManager
    preferencesManager.logout();

    // Clear old SharedPreferences for backward compatibility
    SharedPreferences.Editor editor = prefs.edit();
    editor.clear();
    editor.apply();
  }

  // === UTILITY METHODS ===

  /**
   * Get user info debug string
   */
  public String getUserInfoDebug() {
    return preferencesManager.getUserInfoDebug();
  }

  /**
   * Get PreferencesManager instance
   */
  public PreferencesManager getPreferencesManager() {
    return preferencesManager;
  }
}