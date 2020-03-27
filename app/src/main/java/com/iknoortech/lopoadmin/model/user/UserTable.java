package com.iknoortech.lopoadmin.model.user;

public class UserTable {

    private String userId;
    private String name;
    private String image;
    private String password;
    private String email;
    private String phone;
    private String token;
    private long registrationDate;

    public UserTable() {
    }

    public UserTable(String userId, String name, String image, String password, String email, String phone, String token, long registrationDate) {
        this.userId = userId;
        this.name = name;
        this.image = image;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.token = token;
        this.registrationDate = registrationDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }
}
