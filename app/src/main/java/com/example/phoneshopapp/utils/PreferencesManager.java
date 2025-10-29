package com.example.phoneshopapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * PreferencesManager - Quản lý SharedPreferences cho toàn bộ app
 * Sử dụng cho Login, Register, Profile và các settings khác
 */
public class PreferencesManager {
  private static PreferencesManager instance;
  private static final String PREF_NAME = "PhoneShopApp_Prefs";
  private static final String TAG = "PreferencesManager";

  // User Authentication Keys
  private static final String KEY_USER_ID = "user_id";
  private static final String KEY_USERNAME = "username";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_DISPLAY_NAME = "display_name";
  private static final String KEY_IS_LOGGED_IN = "is_logged_in";
  private static final String KEY_ROLE = "role";
  private static final String KEY_PHONE_NUMBER = "phone_number";
  private static final String KEY_PROFILE_IMAGE_URL = "profile_image_url";

  // ...existing code...

  // App Preferences
  private static final String KEY_THEME_MODE = "theme_mode"; // light, dark, auto
  private static final String KEY_LANGUAGE = "language"; // vi, en
  private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
  private static final String KEY_PUSH_NOTIFICATIONS = "push_notifications";
  private static final String KEY_EMAIL_NOTIFICATIONS = "email_notifications";

  // First Launch & Tutorial
  private static final String KEY_FIRST_LAUNCH = "first_launch";
  private static final String KEY_TUTORIAL_COMPLETED = "tutorial_completed";
  private static final String KEY_APP_VERSION = "app_version";

  private SharedPreferences prefs;

  private PreferencesManager(Context context) {
    prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    Log.d(TAG, "PreferencesManager initialized");
  }

  public static synchronized PreferencesManager getInstance(Context context) {
    if (instance == null) {
      instance = new PreferencesManager(context.getApplicationContext());
    }
    return instance;
  }

  // === USER AUTHENTICATION METHODS ===

  /**
   * Lưu thông tin user đầy đủ sau khi login/register thành công
   */
  public void saveUserSession(String userId, String username, String email, String displayName, String role) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(KEY_USER_ID, userId);
    editor.putString(KEY_USERNAME, username);
    editor.putString(KEY_EMAIL, email);
    editor.putString(KEY_DISPLAY_NAME, displayName);
    editor.putString(KEY_ROLE, role);
    editor.putBoolean(KEY_IS_LOGGED_IN, true);
    // ...existing code...
    editor.apply();

    Log.d(TAG, "User session saved: " + username + " (" + email + ") [" + role + "]");
  }

  /**
   * Lưu thông tin user cơ bản
   */
  public void saveBasicUserInfo(String username, String email) {
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(KEY_USERNAME, username);
    editor.putString(KEY_EMAIL, email);
    editor.putBoolean(KEY_IS_LOGGED_IN, true);
    // ...existing code...
    editor.apply();

    Log.d(TAG, "Basic user info saved: " + username + " (" + email + ")");
  }

  /**
   * Kiểm tra user đã đăng nhập chưa
   */
  public boolean isUserLoggedIn() {
    return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
  }

  /**
   * Lấy User ID
   */
  public String getUserId() {
    return prefs.getString(KEY_USER_ID, "");
  }

  /**
   * Lấy username
   */
  public String getUsername() {
    return prefs.getString(KEY_USERNAME, "");
  }

  /**
   * Lấy email
   */
  public String getUserEmail() {
    return prefs.getString(KEY_EMAIL, "");
  }

  /**
   * Lấy display name
   */
  public String getDisplayName() {
    String displayName = prefs.getString(KEY_DISPLAY_NAME, "");
    if (displayName.isEmpty()) {
      // Fallback to username if display name not set
      return getUsername();
    }
    return displayName;
  }

  /**
   * Lấy role của user
   */
  public String getUserRole() {
    return prefs.getString(KEY_ROLE, "user");
  }

  /**
   * Set role cho user
   */
  public void setUserRole(String role) {
    prefs.edit().putString(KEY_ROLE, role).apply();
    Log.d(TAG, "User role set to: " + role);
  }

  /**
   * Lấy phone number
   */
  public String getPhoneNumber() {
    return prefs.getString(KEY_PHONE_NUMBER, "");
  }

  /**
   * Set phone number
   */
  public void setPhoneNumber(String phoneNumber) {
    prefs.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply();
  }

  /**
   * Lấy profile image URL
   */
  public String getProfileImageUrl() {
    return prefs.getString(KEY_PROFILE_IMAGE_URL, "");
  }

  /**
   * Set profile image URL
   */
  public void setProfileImageUrl(String imageUrl) {
    prefs.edit().putString(KEY_PROFILE_IMAGE_URL, imageUrl).apply();
  }

  /**
   * Update display name
   */
  public void setDisplayName(String displayName) {
    prefs.edit().putString(KEY_DISPLAY_NAME, displayName).apply();
  }

  /**
   * Update email
   */
  public void setEmail(String email) {
    prefs.edit().putString(KEY_EMAIL, email).apply();
  }

  // === LOGIN PREFERENCES ===

  // ...existing code...

  // === APP PREFERENCES ===

  /**
   * Set theme mode
   */
  public void setThemeMode(String theme) {
    prefs.edit().putString(KEY_THEME_MODE, theme).apply();
  }

  /**
   * Get theme mode
   */
  public String getThemeMode() {
    return prefs.getString(KEY_THEME_MODE, "auto");
  }

  /**
   * Set language
   */
  public void setLanguage(String language) {
    prefs.edit().putString(KEY_LANGUAGE, language).apply();
  }

  /**
   * Get language
   */
  public String getLanguage() {
    return prefs.getString(KEY_LANGUAGE, "vi");
  }

  /**
   * Set notifications enabled
   */
  public void setNotificationsEnabled(boolean enabled) {
    prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
  }

  /**
   * Check if notifications enabled
   */
  public boolean areNotificationsEnabled() {
    return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
  }

  /**
   * Set push notifications
   */
  public void setPushNotifications(boolean enabled) {
    prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply();
  }

  /**
   * Check push notifications
   */
  public boolean arePushNotificationsEnabled() {
    return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
  }

  /**
   * Set email notifications
   */
  public void setEmailNotifications(boolean enabled) {
    prefs.edit().putBoolean(KEY_EMAIL_NOTIFICATIONS, enabled).apply();
  }

  /**
   * Check email notifications
   */
  public boolean areEmailNotificationsEnabled() {
    return prefs.getBoolean(KEY_EMAIL_NOTIFICATIONS, true);
  }

  // === FIRST LAUNCH & TUTORIAL ===

  /**
   * Check if this is first launch
   */
  public boolean isFirstLaunch() {
    return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
  }

  /**
   * Set first launch completed
   */
  public void setFirstLaunchCompleted() {
    prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
  }

  /**
   * Check if tutorial completed
   */
  public boolean isTutorialCompleted() {
    return prefs.getBoolean(KEY_TUTORIAL_COMPLETED, false);
  }

  /**
   * Set tutorial completed
   */
  public void setTutorialCompleted() {
    prefs.edit().putBoolean(KEY_TUTORIAL_COMPLETED, true).apply();
  }

  /**
   * Set app version
   */
  public void setAppVersion(String version) {
    prefs.edit().putString(KEY_APP_VERSION, version).apply();
  }

  /**
   * Get app version
   */
  public String getAppVersion() {
    return prefs.getString(KEY_APP_VERSION, "1.0.0");
  }

  // === LOGOUT & CLEANUP ===

  /**
   * Logout - xóa session data nhưng giữ preferences
   */
  public void logout() {
    SharedPreferences.Editor editor = prefs.edit();

    // Xóa user session data
    editor.remove(KEY_USER_ID);
    editor.remove(KEY_USERNAME);
    editor.remove(KEY_EMAIL);
    editor.remove(KEY_DISPLAY_NAME);
    editor.remove(KEY_ROLE);
    editor.remove(KEY_PHONE_NUMBER);
    editor.remove(KEY_PROFILE_IMAGE_URL);
    editor.putBoolean(KEY_IS_LOGGED_IN, false);

    // ...existing code...

    // Giữ lại app preferences (theme, language, etc.)

    editor.apply();
    Log.d(TAG, "User logged out, session cleared");
  }

  /**
   * Clear all preferences (for debug/reset)
   */
  public void clearAllPreferences() {
    prefs.edit().clear().apply();
    Log.d(TAG, "All preferences cleared");
  }

  /**
   * Get all user info as a formatted string (for debugging)
   */
  public String getUserInfoDebug() {
    return "UserInfo{" +
        "id='" + getUserId() + '\'' +
        ", username='" + getUsername() + '\'' +
        ", email='" + getUserEmail() + '\'' +
        ", displayName='" + getDisplayName() + '\'' +
        ", role='" + getUserRole() + '\'' +
        ", isLoggedIn=" + isUserLoggedIn() +
        ", theme='" + getThemeMode() + '\'' +
        ", language='" + getLanguage() + '\'' +
        '}';
  }
}