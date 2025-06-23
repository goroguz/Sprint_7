package com.learn.model;

public class CourierModel {
    private final String login;
    private final String password;
    private final String firstName;

    public CourierModel(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }
}
