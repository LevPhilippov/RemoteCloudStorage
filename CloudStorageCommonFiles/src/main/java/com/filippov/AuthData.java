package com.filippov;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

public class AuthData implements Serializable {

    private String login;
    private String password;
    private boolean newId;

    public AuthData(String login, String password, boolean newId) {
        this.login = DigestUtils.md5Hex(login);
        this.password = DigestUtils.md5Hex(password);
        this.newId = newId;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isNewId() {
        return newId;
    }
}
