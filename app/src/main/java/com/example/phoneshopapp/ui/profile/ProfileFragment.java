package com.example.phoneshopapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import com.example.phoneshopapp.LoginActivity;
import com.example.phoneshopapp.MyOrdersActivity;
import com.example.phoneshopapp.UserManager;
import com.example.phoneshopapp.R;
import com.example.phoneshopapp.databinding.FragmentProfileBinding;
import com.example.phoneshopapp.data.auth.AuthRepository;
import com.example.phoneshopapp.utils.PreferencesManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {
  private static final String TAG = "ProfileFragment";

  private FragmentProfileBinding binding;
  private UserManager userManager;
  private PreferencesManager preferencesManager;

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

    binding = FragmentProfileBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    // Initialize managers
    userManager = UserManager.getInstance(getContext());
    preferencesManager = userManager.getPreferencesManager();

    // Setup user info
    setupUserInfo();

    // Setup click listeners
    setupClickListeners();

    return root;
  }

  private void setupUserInfo() {
    // Hiển thị thông tin user từ PreferencesManager qua UserManager
    String displayName = userManager.getDisplayName();
    String displayEmail = userManager.getDisplayEmail();
    String phoneNumber = userManager.getPhoneNumber();
    String role = userManager.getRole();

    binding.textUserName.setText(displayName);
    binding.textUserEmail.setText(displayEmail);
    binding.textUserRole.setText("Role: " + role);

    // Hiển thị phone number nếu có
    if (!phoneNumber.isEmpty()) {
      // Nếu có TextView cho phone trong layout
      // binding.textUserPhone.setText(phoneNumber);
    }

    // Avatar initial: ký tự đầu của tên hiển thị (viết hoa)
    String initial = "?";
    if (displayName != null && displayName.trim().length() > 0) {
      initial = displayName.trim().substring(0, 1).toUpperCase();
    } else if (displayEmail != null && displayEmail.length() > 0) {
      initial = displayEmail.substring(0, 1).toUpperCase();
    }
    binding.textAvatarInitial.setText(initial);

    // Hide Verify Email when already verified
    AuthRepository repo = new AuthRepository();
    FirebaseUser current = repo.getCurrentUser();
    if (current != null && current.isEmailVerified()) {
      binding.layoutVerifyEmail.setVisibility(View.GONE);
    } else {
      binding.layoutVerifyEmail.setVisibility(View.VISIBLE);
    }

    Log.d(TAG, "User info displayed: " + userManager.getUserInfoDebug());
  }

  private void setupClickListeners() {
    // Edit Profile
    binding.layoutEditProfile.setOnClickListener(v -> {
      showEditProfileDialog();
    });

    // Change Password
    binding.layoutChangePassword.setOnClickListener(v -> {
      // Gửi email đặt lại mật khẩu đến email hiện tại
      AuthRepository authRepository = new AuthRepository();
      FirebaseUser current = authRepository.getCurrentUser();
      if (current != null && current.getEmail() != null) {
        authRepository.sendPasswordReset(current.getEmail())
            .addOnSuccessListener(unused -> Toast
                .makeText(getContext(), "Reset email sent to " + current.getEmail(), Toast.LENGTH_LONG).show())
            .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
      } else {
        Toast.makeText(getContext(), "No signed-in user email", Toast.LENGTH_SHORT).show();
      }
    });

    // Order History
    binding.layoutOrderHistory.setOnClickListener(v -> {
      Intent intent = new Intent(getActivity(), MyOrdersActivity.class);
      startActivity(intent);
    });

    // My Favorites
    binding.layoutMyFavorites.setOnClickListener(v -> {
      Intent intent = new Intent(getActivity(), com.example.phoneshopapp.ui.favorites.FavoritesActivity.class);
      startActivity(intent);
    });

    // Manage Addresses
    binding.layoutManageAddresses.setOnClickListener(v -> {
      Intent intent = new Intent(getActivity(), com.example.phoneshopapp.ManageAddressesActivity.class);
      startActivity(intent);
    });

    // Notifications
    binding.layoutNotifications.setOnClickListener(v -> {
      showNotificationSettingsDialog();
    });

    // Verify Email
    binding.layoutVerifyEmail.setOnClickListener(v -> {
      AuthRepository authRepository = new AuthRepository();
      FirebaseUser current = authRepository.getCurrentUser();
      if (current == null) {
        Toast.makeText(getContext(), "No signed-in user", Toast.LENGTH_SHORT).show();
        return;
      }
      if (current.isEmailVerified()) {
        Toast.makeText(getContext(), "Email already verified", Toast.LENGTH_SHORT).show();
        return;
      }
      current.sendEmailVerification()
          .addOnSuccessListener(
              unused -> Toast.makeText(getContext(), "Verification email sent", Toast.LENGTH_LONG).show())
          .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    });

    // Logout
    binding.btnLogout.setOnClickListener(v -> {
      showLogoutConfirmDialog();
    });
  }

  private void showNotificationSettingsDialog() {
    if (getContext() == null)
      return;

    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notification_settings, null);

    // Find switches - create if not exist
    Switch switchNotifications = dialogView.findViewById(R.id.switchNotifications);
    Switch switchPushNotifications = dialogView.findViewById(R.id.switchPushNotifications);
    Switch switchEmailNotifications = dialogView.findViewById(R.id.switchEmailNotifications);

    // Set current values
    if (switchNotifications != null) {
      switchNotifications.setChecked(preferencesManager.areNotificationsEnabled());
    }
    if (switchPushNotifications != null) {
      switchPushNotifications.setChecked(preferencesManager.arePushNotificationsEnabled());
    }
    if (switchEmailNotifications != null) {
      switchEmailNotifications.setChecked(preferencesManager.areEmailNotificationsEnabled());
    }

    AlertDialog dialog = new AlertDialog.Builder(requireContext())
        .setTitle("Notification Settings")
        .setView(dialogView)
        .setPositiveButton("Save", (d, which) -> {
          // Save settings
          if (switchNotifications != null) {
            preferencesManager.setNotificationsEnabled(switchNotifications.isChecked());
          }
          if (switchPushNotifications != null) {
            preferencesManager.setPushNotifications(switchPushNotifications.isChecked());
          }
          if (switchEmailNotifications != null) {
            preferencesManager.setEmailNotifications(switchEmailNotifications.isChecked());
          }

          Toast.makeText(getContext(), "Notification settings saved", Toast.LENGTH_SHORT).show();
        })
        .setNegativeButton("Cancel", null)
        .create();

    dialog.show();
  }

  private void showLogoutConfirmDialog() {
    if (getContext() == null)
      return;

    new AlertDialog.Builder(requireContext())
        .setTitle("Logout")
        .setMessage("Are you sure you want to logout?")
        .setPositiveButton("Logout", (dialog, which) -> {
          performLogout();
        })
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void performLogout() {
    Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

    Log.d(TAG, "User logging out: " + userManager.getUserInfoDebug());

    // Sign out from Firebase + clear local
    new AuthRepository().logout();
    userManager.logout();

    // Navigate back to login screen
    Intent intent = new Intent(getActivity(), LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);

    if (getActivity() != null) {
      getActivity().finish();
    }
  }

  private void showEditProfileDialog() {
    if (getContext() == null)
      return;
    AuthRepository repo = new AuthRepository();
    FirebaseUser current = repo.getCurrentUser();
    if (current == null) {
      Toast.makeText(getContext(), "No signed-in user", Toast.LENGTH_SHORT).show();
      return;
    }

    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_profile, null);
    TextView inputFullName = dialogView.findViewById(R.id.inputFullName);
    TextView inputPhone = dialogView.findViewById(R.id.inputPhone);

    // Prefill with existing Firestore values
    repo.fetchUserProfile(current.getUid())
        .addOnSuccessListener(doc -> {
          if (doc != null && doc.exists()) {
            String fullName = doc.getString("fullName");
            String phone = doc.getString("phone");
            if (fullName != null)
              inputFullName.setText(fullName);
            if (phone != null)
              inputPhone.setText(phone);
          }
        });

    AlertDialog dialog = new AlertDialog.Builder(requireContext())
        .setView(dialogView)
        .create();

    Button btnCancel = dialogView.findViewById(R.id.btnCancel);
    Button btnSave = dialogView.findViewById(R.id.btnSave);

    btnCancel.setOnClickListener(v -> dialog.dismiss());
    btnSave.setOnClickListener(v -> {
      String fullName = inputFullName.getText().toString().trim();
      String phone = inputPhone.getText().toString().trim();
      if (fullName.isEmpty()) {
        Toast.makeText(getContext(), "Full name is required", Toast.LENGTH_SHORT).show();
        return;
      }
      // Update Firestore
      repo.updateUserProfile(current.getUid(), fullName, phone)
          .addOnSuccessListener(unused -> {
            // Update local using UserManager enhanced methods
            userManager.setDisplayName(fullName);
            userManager.setPhoneNumber(phone);

            // Update UI
            binding.textUserName.setText(fullName);
            // Update avatar initial
            String initial = fullName.substring(0, 1).toUpperCase();
            binding.textAvatarInitial.setText(initial);

            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            Log.d(TAG, "Profile updated: " + userManager.getUserInfoDebug());
          })
          .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    });

    dialog.show();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
