package com.example.phoneshopapp.utils;

import android.content.Context;
import android.util.Log;
import com.example.phoneshopapp.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthDebugHelper {
  private static final String TAG = "AuthDebug";

  public static void debugAuthState(Context context) {
    Log.d(TAG, "=== Auth Debug Info ===");

    // Check Firebase Auth
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    Log.d(TAG, "Firebase Auth current user: " + (currentUser != null ? currentUser.getUid() : "null"));
    if (currentUser != null) {
      Log.d(TAG, "User email: " + currentUser.getEmail());
      Log.d(TAG, "User display name: " + currentUser.getDisplayName());
      Log.d(TAG, "Email verified: " + currentUser.isEmailVerified());
    }

    // Check UserManager
    UserManager userManager = UserManager.getInstance(context);
    Log.d(TAG, "UserManager isLoggedIn: " + userManager.isLoggedIn());
    Log.d(TAG, "UserManager username: " + userManager.getUsername());
    Log.d(TAG, "UserManager email: " + userManager.getUserEmail());
    Log.d(TAG, "UserManager getCurrentUserId: " + userManager.getCurrentUserId());
    Log.d(TAG, "UserManager role: " + userManager.getRole());

    Log.d(TAG, "=====================");
  }

  public static boolean isUserFullyAuthenticated(Context context) {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    UserManager userManager = UserManager.getInstance(context);

    boolean firebaseAuth = currentUser != null;
    boolean userManagerAuth = userManager.isLoggedIn();
    boolean hasUserId = userManager.getCurrentUserId() != null;

    Log.d(TAG,
        "Auth check - Firebase: " + firebaseAuth + ", UserManager: " + userManagerAuth + ", UserId: " + hasUserId);

    return firebaseAuth && userManagerAuth && hasUserId;
  }
}