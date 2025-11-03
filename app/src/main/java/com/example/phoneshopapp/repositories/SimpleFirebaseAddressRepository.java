package com.example.phoneshopapp.repositories;

import android.util.Log;
import com.example.phoneshopapp.models.Address;
import com.example.phoneshopapp.repositories.callbacks.AddressesCallback;
import com.example.phoneshopapp.repositories.callbacks.AddressCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple Firebase Firestore implementation for address storage
 */
public class SimpleFirebaseAddressRepository implements AddressRepository {
    
    private static final String TAG = "SimpleFirebaseAddressRepo";
    private static final String COLLECTION_ADDRESSES = "addresses";
    
    private final FirebaseFirestore db;
    
    public SimpleFirebaseAddressRepository() {
        try {
            Log.d(TAG, "Initializing FirebaseFirestore instance");
            this.db = FirebaseFirestore.getInstance();
            if (this.db == null) {
                throw new RuntimeException("FirebaseFirestore.getInstance() returned null");
            }
            Log.d(TAG, "FirebaseFirestore initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize FirebaseFirestore", e);
            throw new RuntimeException("Failed to initialize FirebaseFirestore", e);
        }
    }
    
    @Override
    public void getUserAddresses(String userId, AddressesCallback callback) {
        try {
            db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Address> addresses = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Address address = documentToAddress(document);
                            if (address != null) {
                                addresses.add(address);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing address document", e);
                        }
                    }
                    
                    callback.onSuccess(addresses);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting addresses", e);
                    callback.onError("Lỗi khi tải địa chỉ: " + e.getMessage());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in getUserAddresses", e);
            callback.onError("Lỗi khi tải địa chỉ: " + e.getMessage());
        }
    }
    
    @Override
    public void addAddress(Address address, UpdateCallback callback) {
        try {
            // Generate ID if not present
            if (address.getAddressId() == null || address.getAddressId().isEmpty()) {
                address.setAddressId(UUID.randomUUID().toString());
            }
            
            // Set timestamps
            Date now = new Date();
            address.setCreatedAt(now);
            address.setUpdatedAt(now);
            
            // If this is set as default, handle other addresses first
            if (address.isDefault()) {
                setOtherAddressesAsNonDefault(address.getUserId(), () -> {
                    saveAddressToFirestore(address, callback);
                }, callback);
            } else {
                saveAddressToFirestore(address, callback);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in addAddress", e);
            callback.onError("Lỗi khi thêm địa chỉ: " + e.getMessage());
        }
    }
    
    @Override
    public void updateAddress(Address address, UpdateCallback callback) {
        try {
            // Update timestamp
            address.setUpdatedAt(new Date());
            
            // If this is set as default, handle other addresses first
            if (address.isDefault()) {
                setOtherAddressesAsNonDefault(address.getUserId(), () -> {
                    updateAddressInFirestore(address, callback);
                }, callback);
            } else {
                updateAddressInFirestore(address, callback);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in updateAddress", e);
            callback.onError("Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteAddress(String addressId, UpdateCallback callback) {
        try {
            db.collection(COLLECTION_ADDRESSES)
                .document(addressId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Address deleted successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting address", e);
                    callback.onError("Lỗi khi xóa địa chỉ: " + e.getMessage());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteAddress", e);
            callback.onError("Lỗi khi xóa địa chỉ: " + e.getMessage());
        }
    }
    
    @Override
    public void setDefaultAddress(String userId, String addressId, UpdateCallback callback) {
        try {
            // First, set all addresses as non-default
            setOtherAddressesAsNonDefault(userId, () -> {
                // Then set the specified address as default
                db.collection(COLLECTION_ADDRESSES)
                    .document(addressId)
                    .update("isDefault", true)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Default address set successfully");
                        callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error setting default address", e);
                        callback.onError("Lỗi khi set địa chỉ mặc định: " + e.getMessage());
                    });
            }, callback);
            
        } catch (Exception e) {
            Log.e(TAG, "Error in setDefaultAddress", e);
            callback.onError("Lỗi khi set địa chỉ mặc định: " + e.getMessage());
        }
    }
    
    @Override
    public void getDefaultAddress(String userId, AddressCallback callback) {
        try {
            db.collection(COLLECTION_ADDRESSES)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDefault", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Address address = documentToAddress(document);
                        if (address != null) {
                            callback.onSuccess(address);
                        } else {
                            callback.onError("Lỗi khi parse địa chỉ mặc định");
                        }
                    } else {
                        callback.onError("Không có địa chỉ mặc định");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting default address", e);
                    callback.onError("Lỗi khi lấy địa chỉ mặc định: " + e.getMessage());
                });
                
        } catch (Exception e) {
            Log.e(TAG, "Error in getDefaultAddress", e);
            callback.onError("Lỗi khi lấy địa chỉ mặc định: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private void saveAddressToFirestore(Address address, UpdateCallback callback) {
        Map<String, Object> addressData = addressToMap(address);
        
        db.collection(COLLECTION_ADDRESSES)
            .document(address.getAddressId())
            .set(addressData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Address saved successfully");
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error saving address", e);
                callback.onError("Lỗi khi lưu địa chỉ: " + e.getMessage());
            });
    }
    
    private void updateAddressInFirestore(Address address, UpdateCallback callback) {
        Map<String, Object> addressData = addressToMap(address);
        
        db.collection(COLLECTION_ADDRESSES)
            .document(address.getAddressId())
            .update(addressData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Address updated successfully");
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating address", e);
                callback.onError("Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            });
    }
    
    private void setOtherAddressesAsNonDefault(String userId, Runnable onSuccess, UpdateCallback callback) {
        db.collection(COLLECTION_ADDRESSES)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDefault", true)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    onSuccess.run();
                    return;
                }
                
                // Count how many updates we need to complete
                final int totalUpdates = queryDocumentSnapshots.size();
                final int[] completedUpdates = {0};
                
                // Update all existing default addresses to non-default
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    db.collection(COLLECTION_ADDRESSES)
                        .document(document.getId())
                        .update("isDefault", false)
                        .addOnSuccessListener(aVoid -> {
                            completedUpdates[0]++;
                            if (completedUpdates[0] == totalUpdates) {
                                onSuccess.run();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Failed to update address to non-default: " + e.getMessage());
                            completedUpdates[0]++;
                            if (completedUpdates[0] == totalUpdates) {
                                onSuccess.run();
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error setting other addresses as non-default", e);
                callback.onError("Lỗi khi cập nhật địa chỉ mặc định: " + e.getMessage());
            });
    }
    
    private Map<String, Object> addressToMap(Address address) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", address.getUserId());
        data.put("addressName", address.getAddressName());
        data.put("recipientName", address.getRecipientName());
        data.put("phone", address.getPhone());
        data.put("addressDetails", address.getAddressDetails());
        data.put("ward", address.getWard());
        data.put("district", address.getDistrict());
        data.put("province", address.getProvince());
        data.put("fullAddress", address.getFullAddress());
        data.put("isDefault", address.isDefault());
        data.put("createdAt", address.getCreatedAt());
        data.put("updatedAt", address.getUpdatedAt());
        return data;
    }
    
    private Address documentToAddress(QueryDocumentSnapshot document) {
        try {
            Address address = new Address();
            address.setAddressId(document.getId());
            address.setUserId(document.getString("userId"));
            address.setAddressName(document.getString("addressName"));
            address.setRecipientName(document.getString("recipientName"));
            address.setPhone(document.getString("phone"));
            address.setAddressDetails(document.getString("addressDetails"));
            address.setWard(document.getString("ward"));
            address.setDistrict(document.getString("district"));
            address.setProvince(document.getString("province"));
            address.setFullAddress(document.getString("fullAddress"));
            address.setDefault(document.getBoolean("isDefault") != null ? document.getBoolean("isDefault") : false);
            address.setCreatedAt(document.getDate("createdAt"));
            address.setUpdatedAt(document.getDate("updatedAt"));
            return address;
        } catch (Exception e) {
            Log.e(TAG, "Error converting document to Address", e);
            return null;
        }
    }
}