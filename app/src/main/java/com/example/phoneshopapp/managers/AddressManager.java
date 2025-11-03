package com.example.phoneshopapp.managers;

import android.content.Context;
import android.util.Log;

import com.example.phoneshopapp.models.Address;
import com.example.phoneshopapp.repositories.AddressRepository;
import com.example.phoneshopapp.repositories.callbacks.AddressesCallback;
import com.example.phoneshopapp.repositories.callbacks.AddressCallback;
import com.example.phoneshopapp.repositories.callbacks.UpdateCallback;

import java.util.Date;
import java.util.List;

/**
 * Singleton class quản lý địa chỉ giao hàng
 * Handles address management operations
 */
public class AddressManager {
    private static final String TAG = "AddressManager";
    
    private static AddressManager instance;
    private final AddressRepository addressRepository;
    private final Context context;

    private AddressManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            // Use simple Firebase implementation instead of null
            Log.d(TAG, "Initializing SimpleFirebaseAddressRepository");
            this.addressRepository = new com.example.phoneshopapp.repositories.SimpleFirebaseAddressRepository();
            Log.d(TAG, "SimpleFirebaseAddressRepository initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SimpleFirebaseAddressRepository", e);
            throw new RuntimeException("Failed to initialize AddressRepository", e);
        }
    }

    public static synchronized AddressManager getInstance(Context context) {
        if (instance == null) {
            instance = new AddressManager(context);
        }
        return instance;
    }

    /**
     * Get all addresses for current user
     * @param callback Callback for success/error handling
     */
    public void getUserAddresses(AddressesCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }
        
        addressRepository.getUserAddresses(userId, callback);
    }

    /**
     * Add a new address
     * @param addressName Name/label for the address
     * @param recipientName Name of the recipient
     * @param phone Phone number
     * @param addressDetails Detailed address (street, number)
     * @param ward Ward/Commune
     * @param district District
     * @param province Province/City
     * @param isDefault Whether this should be the default address
     * @param callback Callback for success/error handling
     */
    public void addAddress(String addressName, String recipientName, String phone,
                          String addressDetails, String ward, String district, String province,
                          boolean isDefault, UpdateCallback callback) {
        
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        // Validate input
        if (!isValidAddressInput(addressName, recipientName, phone, addressDetails, ward, district, province)) {
            callback.onError("Thông tin địa chỉ không hợp lệ");
            return;
        }

        // Create new address
        Address address = new Address();
        address.setUserId(userId);
        address.setAddressName(addressName);
        address.setRecipientName(recipientName);
        address.setPhone(phone);
        address.setAddressDetails(addressDetails);
        address.setWard(ward);
        address.setDistrict(district);
        address.setProvince(province);
        address.setFullAddress(address.generateFullAddress());
        address.setDefault(isDefault);
        
        Date now = new Date();
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        address.setFullAddress(address.generateFullAddress());

        // If this is set as default, we need to handle setting other addresses as non-default
        if (isDefault) {
            setOtherAddressesAsNonDefault(userId, address, callback);
        } else {
            addressRepository.addAddress(address, callback);
        }
    }

    /**
     * Update an existing address
     * @param address Address object to update
     * @param callback Callback for success/error handling
     */
    public void updateAddress(Address address, UpdateCallback callback) {
        if (address == null || address.getAddressId() == null) {
            callback.onError("Thông tin địa chỉ không hợp lệ");
            return;
        }

        // Validate input
        if (!isValidAddress(address)) {
            callback.onError("Thông tin địa chỉ không hợp lệ");
            return;
        }

        // Update timestamp and regenerate full address
        address.setUpdatedAt(new Date());
        address.setFullAddress(address.generateFullAddress());

        // If this is set as default, handle other addresses
        if (address.isDefault()) {
            setOtherAddressesAsNonDefault(address.getUserId(), address, callback);
        } else {
            addressRepository.updateAddress(address, callback);
        }
    }

    /**
     * Delete an address
     * @param addressId Address ID to delete
     * @param callback Callback for success/error handling
     */
    public void deleteAddress(String addressId, UpdateCallback callback) {
        if (addressId == null || addressId.trim().isEmpty()) {
            callback.onError("Mã địa chỉ không hợp lệ");
            return;
        }

        addressRepository.deleteAddress(addressId, callback);
    }

    /**
     * Set an address as default
     * @param addressId Address ID to set as default
     * @param callback Callback for success/error handling
     */
    public void setDefaultAddress(String addressId, UpdateCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        if (addressId == null || addressId.trim().isEmpty()) {
            callback.onError("Mã địa chỉ không hợp lệ");
            return;
        }

        addressRepository.setDefaultAddress(userId, addressId, callback);
    }

    /**
     * Get the default address for current user
     * @param callback Callback for success/error handling
     */
    public void getDefaultAddress(AddressCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        addressRepository.getDefaultAddress(userId, callback);
    }

    /**
     * Create mock addresses for testing
     * @param callback Callback for success/error handling
     */
    public void createMockAddresses(UpdateCallback callback) {
        String userId = getUserId();
        if (userId == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        // Create home address
        addAddress(
            "Nhà riêng",
            "Nguyễn Văn A",
            "0912345678",
            "123 Đường Lê Lợi",
            "Phường Bến Nghé",
            "Quận 1",
            "TP. Hồ Chí Minh",
            true,
            new UpdateCallback() {
                @Override
                public void onSuccess() {
                    // Create office address
                    addAddress(
                        "Công ty",
                        "Nguyễn Văn A",
                        "0987654321",
                        "456 Đường Nguyễn Huệ",
                        "Phường Bến Nghé",
                        "Quận 1",
                        "TP. Hồ Chí Minh",
                        false,
                        callback
                    );
                }

                @Override
                public void onError(String errorMessage) {
                    callback.onError(errorMessage);
                }
            }
        );
    }

    // Helper methods

    private boolean isValidAddressInput(String addressName, String recipientName, String phone,
                                       String addressDetails, String ward, String district, String province) {
        return addressName != null && !addressName.trim().isEmpty() &&
               recipientName != null && !recipientName.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               addressDetails != null && !addressDetails.trim().isEmpty() &&
               ward != null && !ward.trim().isEmpty() &&
               district != null && !district.trim().isEmpty() &&
               province != null && !province.trim().isEmpty();
    }

    private boolean isValidAddress(Address address) {
        return address.getAddressName() != null && !address.getAddressName().trim().isEmpty() &&
               address.getRecipientName() != null && !address.getRecipientName().trim().isEmpty() &&
               address.getPhone() != null && !address.getPhone().trim().isEmpty() &&
               address.getAddressDetails() != null && !address.getAddressDetails().trim().isEmpty() &&
               address.getWard() != null && !address.getWard().trim().isEmpty() &&
               address.getDistrict() != null && !address.getDistrict().trim().isEmpty() &&
               address.getProvince() != null && !address.getProvince().trim().isEmpty();
    }

    private void setOtherAddressesAsNonDefault(String userId, Address newDefaultAddress, UpdateCallback callback) {
        // First get all user addresses
        getUserAddresses(new AddressesCallback() {
            @Override
            public void onSuccess(List<Address> addresses) {
                // Set all other addresses as non-default
                for (Address addr : addresses) {
                    if (!addr.getAddressId().equals(newDefaultAddress.getAddressId())) {
                        addr.setDefault(false);
                        addr.setUpdatedAt(new Date());
                        // Update each address (this is simplified, in real app might use batch update)
                        addressRepository.updateAddress(addr, new UpdateCallback() {
                            @Override
                            public void onSuccess() {
                                // Individual address updated
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.w(TAG, "Failed to update address: " + errorMessage);
                            }
                        });
                    }
                }
                
                // Now add/update the new default address
                if (newDefaultAddress.getAddressId() == null) {
                    addressRepository.addAddress(newDefaultAddress, callback);
                } else {
                    addressRepository.updateAddress(newDefaultAddress, callback);
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError("Lỗi cập nhật địa chỉ mặc định: " + errorMessage);
            }
        });
    }

    private String getUserId() {
        // Get current user ID from UserManager
        com.example.phoneshopapp.UserManager userManager = 
                com.example.phoneshopapp.UserManager.getInstance(context);
        return userManager.getCurrentUserId();
    }

    /**
     * Format address for display
     * @param address Address to format
     * @return Formatted address string
     */
    public String formatAddressForDisplay(Address address) {
        if (address == null) return "";
        
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressName()).append("\n");
        sb.append(address.getRecipientName()).append(" - ").append(address.getPhone()).append("\n");
        sb.append(address.getFullAddress());
        
        return sb.toString();
    }

    /**
     * Get short address format for spinner/dropdown
     * @param address Address to format
     * @return Short formatted address string
     */
    public String formatAddressForSpinner(Address address) {
        if (address == null) return "";
        
        return address.getAddressName() + " - " + address.getAddressDetails() + ", " + address.getDistrict();
    }
}