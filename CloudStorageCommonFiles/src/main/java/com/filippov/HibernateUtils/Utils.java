package com.filippov.HibernateUtils;

import com.filippov.AuthData;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;


public class Utils {


    public static void writeNewClientAuthData(AuthData authData) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        AuthDataEntity authDataEntity = new AuthDataEntity();
        authDataEntity.setLogin(authData.getLogin());
        authDataEntity.setPassword(authData.getPassword());
        session.save(authDataEntity);
        session.getTransaction().commit();
        session.close();
    }

    public static boolean checkAuthData(AuthData authData) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();

        Query query = session.createQuery("from AuthDataEntity where login=:paramName1 AND password =:paramName2");
        query.setParameter("paramName1", authData.getLogin());
        query.setParameter("paramName2", authData.getPassword());
        List list = query.list();
        session.close();
        System.out.println(list);
        return !list.isEmpty();
    }
}
