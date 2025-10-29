package com.example.phoneshopapp.data.auth;

import android.app.Activity;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.example.phoneshopapp.models.AppUser;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
  private final FirebaseAuth auth;
  private final FirebaseFirestore db;

  public AuthRepository() {
    auth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
  }

  public FirebaseUser getCurrentUser() {
    return auth.getCurrentUser();
  }

  public Task<AuthResult> loginWithEmail(String email, String password) {
    return auth.signInWithEmailAndPassword(email, password);
  }

  public Task<AuthResult> registerWithEmail(String email, String password, String fullName, String phone) {
    return auth.createUserWithEmailAndPassword(email, password)
        .onSuccessTask(result -> {
          FirebaseUser user = result.getUser();
          if (user == null) {
            throw new IllegalStateException("User creation succeeded but user is null");
          }
          // Write profile to Firestore
          String uid = user.getUid();
          Map<String, Object> data = new HashMap<>();
          data.put("uid", uid);
          data.put("fullName", fullName);
          data.put("email", email);
          data.put("phone", phone);
          data.put("role", "user");
          data.put("createdAt", Timestamp.now());

          DocumentReference ref = db.collection("users").document(uid);
          return ref.set(data).continueWithTask(task -> {
            if (!task.isSuccessful()) {
              // Bubble up Firestore failure
              throw task.getException();
            }
            return Tasks.forResult(result);
          });
        });
  }

  public void logout() {
    auth.signOut();
  }

  public Task<Void> sendPasswordReset(@NonNull String email) {
    return auth.sendPasswordResetEmail(email);
  }

  /**
   * Fetch the role of a user from Firestore. Returns "user" if not set.
   */
  public Task<String> getUserRole(@NonNull String uid) {
    return db.collection("users").document(uid)
        .get()
        .continueWith(task -> {
          if (!task.isSuccessful()) {
            throw task.getException();
          }
          DocumentSnapshot doc = task.getResult();
          if (doc != null && doc.exists()) {
            String role = doc.getString("role");
            return (role != null && !role.trim().isEmpty()) ? role : "user";
          }
          return "user";
        });
  }

  public Task<DocumentSnapshot> fetchUserProfile(@NonNull String uid) {
    return db.collection("users").document(uid).get();
  }

  public Task<Void> updateUserProfile(@NonNull String uid, @NonNull String fullName, @NonNull String phone) {
    Map<String, Object> updates = new HashMap<>();
    updates.put("fullName", fullName);
    updates.put("phone", phone);
    DocumentReference ref = db.collection("users").document(uid);
    return ref.update(updates);
  }

  public static String validateEmailPassword(String email, String password) {
    if (email == null || email.trim().isEmpty())
      return "Email is required";
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
      return "Invalid email format";
    if (password == null || password.length() < 6)
      return "Password must be at least 6 characters";
    return null;
  }
}
