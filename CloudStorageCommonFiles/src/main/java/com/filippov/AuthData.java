package com.filippov;


import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;

public class AuthData implements Serializable {

    private String login;
    private String password;

    public AuthData(String login, String password) {
        this.login = DigestUtils.md5Hex(login);
        this.password = DigestUtils.md5Hex(password);
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
