package com.filippov;


import java.io.Serializable;

public class AuthData implements Serializable {

    private long loginHash;
    private long passwordHash;

    public AuthData(String login, String password) {
        this.loginHash = login.hashCode();
        this.passwordHash = password.hashCode();
    }

    public long getLoginHash() {
        return loginHash;
    }

    public long getPasswordHash() {
        return passwordHash;
    }
}
