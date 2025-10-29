package com.example.phoneshopapp.models;

import java.util.Date;

public class Address {
    private String addressId;
    private String userId;
    private String addressName; // Tên địa chỉ (Nhà riêng, Công ty, etc.)
    private String recipientName;
    private String phone;
    private String addressDetails; // Số nhà, đường
    private String ward; // Phường/Xã
    private String district; // Quận/Huyện
    private String province; // Tỉnh/Thành phố
    private String fullAddress; // Địa chỉ đầy đủ (tự động generate)
    private boolean isDefault;
    private Date createdAt;
    private Date updatedAt;

    public Address() {
        // Default constructor required for calls to DataSnapshot.getValue(Address.class)
    }

    public Address(String userId, String addressName, String recipientName, String phone,
                  String addressDetails, String ward, String district, String province) {
        this.userId = userId;
        this.addressName = addressName;
        this.recipientName = recipientName;
        this.phone = phone;
        this.addressDetails = addressDetails;
        this.ward = ward;
        this.district = district;
        this.province = province;
        this.fullAddress = generateFullAddress();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and setters
    public String getAddressId() { return addressId; }
    public void setAddressId(String addressId) { this.addressId = addressId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddressDetails() { return addressDetails; }
    public void setAddressDetails(String addressDetails) { 
        this.addressDetails = addressDetails;
        this.fullAddress = generateFullAddress();
    }

    public String getWard() { return ward; }
    public void setWard(String ward) { 
        this.ward = ward;
        this.fullAddress = generateFullAddress();
    }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { 
        this.district = district;
        this.fullAddress = generateFullAddress();
    }

    public String getProvince() { return province; }
    public void setProvince(String province) { 
        this.province = province;
        this.fullAddress = generateFullAddress();
    }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean defaultAddress) { isDefault = defaultAddress; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public String generateFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressDetails != null && !addressDetails.trim().isEmpty()) {
            sb.append(addressDetails);
        }
        if (ward != null && !ward.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (district != null && !district.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (province != null && !province.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        return sb.toString();
    }

    public String getDisplayName() {
        return addressName != null ? addressName : "Địa chỉ";
    }

    public String getFormattedPhone() {
        if (phone == null || phone.trim().isEmpty()) {
            return "N/A";
        }
        // Format: 0xxx xxx xxx
        if (phone.length() == 10 && phone.startsWith("0")) {
            return phone.substring(0, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);
        }
        return phone;
    }

    public boolean isValid() {
        return recipientName != null && !recipientName.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               addressDetails != null && !addressDetails.trim().isEmpty() &&
               ward != null && !ward.trim().isEmpty() &&
               district != null && !district.trim().isEmpty() &&
               province != null && !province.trim().isEmpty();
    }

    public void updateTimestamp() {
        this.updatedAt = new Date();
    }
}