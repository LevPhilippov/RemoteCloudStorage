package com.filippov.HibernateUtils;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "authdatatable", schema = "cloudstoragedb", catalog = "")
public class AuthDataEntity {
    private String login;
    private String password;

    @Basic
    @Id
    @Column(name = "login", nullable = true, length = 128)
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Basic
    @Column(name = "password", nullable = true, length = 128)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthDataEntity that = (AuthDataEntity) o;
        return Objects.equals(login, that.login) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
