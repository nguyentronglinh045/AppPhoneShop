package com.example.phoneshopapp;

public class Category {
  private String name;
  private int iconResourceId;

  public Category(String name, int iconResourceId) {
    this.name = name;
    this.iconResourceId = iconResourceId;
  }

  public String getName() {
    return name;
  }

  public int getIconResourceId() {
    return iconResourceId;
  }
}