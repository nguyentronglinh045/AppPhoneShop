package com.example.phoneshopapp.models;

public class Banner {
    private String title;
    private String subtitle;
    private String ctaText;
    private int imageResource;
    private String actionUrl;
    
    public Banner() {
        // Empty constructor for Firebase
    }
    
    public Banner(String title, String subtitle, String ctaText, int imageResource) {
        this.title = title;
        this.subtitle = subtitle;
        this.ctaText = ctaText;
        this.imageResource = imageResource;
    }
    
    public Banner(String title, String subtitle, String ctaText, int imageResource, String actionUrl) {
        this.title = title;
        this.subtitle = subtitle;
        this.ctaText = ctaText;
        this.imageResource = imageResource;
        this.actionUrl = actionUrl;
    }
    
    // Getters
    public String getTitle() {
        return title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public String getCtaText() {
        return ctaText;
    }
    
    public int getImageResource() {
        return imageResource;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    // Setters
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }
    
    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}