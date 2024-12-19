package com.example.phone_contact;

public class Contact {
    private String name;
    private String email;
    private String phoneNumber;
    private int imageResource;

    // Constructor
    public Contact(String name, String email, String phoneNumber, int imageResource) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.imageResource = imageResource;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getImageResource() {
        return imageResource;
    }
}
