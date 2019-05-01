package com.filippov.HibernateUtils;

import com.filippov.AuthData;
import org.hibernate.Session;

public class Utils {

    public static void write(AuthData authData) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AuthDataEntity authDataEntity = new AuthDataEntity();
        authDataEntity.setLogin(authData.getLogin());
        authDataEntity.setPassword(authData.getPassword());
        session.save(authDataEntity);
        session.getTransaction().commit();
        session.close();
    }
}
