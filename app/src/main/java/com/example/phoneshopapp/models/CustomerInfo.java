package com.example.phoneshopapp.models;

public class CustomerInfo {
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String note;

    public CustomerInfo() {
        // Default constructor required for calls to DataSnapshot.getValue(CustomerInfo.class)
    }

    public CustomerInfo(String fullName, String phone, String email, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public CustomerInfo(String fullName, String phone, String email, String address, String note) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
    }

    // Getters and setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    // Helper methods
    public boolean isValid() {
        return fullName != null && !fullName.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               address != null && !address.trim().isEmpty();
    }

    public String getDisplayName() {
        return fullName != null ? fullName : "N/A";
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
}