package com.example.suitcase.user;

public class users {
    protected String email;
    protected String password;

    public users() {
    }

    public users(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "users{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
