package com.example.phoneshopapp;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Application class để khởi tạo Firebase và các services cần thiết
 */
public class PhoneShopApplication extends Application {
    private static final String TAG = "PhoneShopApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application onCreate started");
        
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
            
            // Configure Firestore settings
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
            firestore.setFirestoreSettings(settings);
            
            Log.d(TAG, "Firestore configured successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize Firebase", e);
        }
        
        Log.d(TAG, "Application onCreate completed");
    }
}
