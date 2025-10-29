package com.example.phoneshopapp.models;

import java.util.Date;

/**
 * AppUser - User profile stored in Firestore (collection: users)
 */
public class AppUser {
  private String uid;
  private String fullName;
  private String email;
  private String phone;
  private String role; // e.g., "user" | "admin"
  private Date createdAt;

  public AppUser() {
    // Firestore requires a public no-arg constructor
  }

  public AppUser(String uid, String fullName, String email, String phone) {
    this.uid = uid;
    this.fullName = fullName;
    this.email = email;
    this.phone = phone;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
