package com.filippov.HibernateUtils;

import com.filippov.AuthData;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Utils {


//    public static void writeNewClientAuthData(AuthData authData) {
//        Session session = HibernateSessionFactory.getSessionFactory().openSession();
//        session.beginTransaction();
//        ///
//        AuthDataEntity authDataEntity = new AuthDataEntity();
//        authDataEntity.setLogin(authData.getLogin());
//        authDataEntity.setPassword(authData.getPassword());
//        ///
//        session.save(authDataEntity);
//        session.getTransaction().commit();
//        session.close();
//    }

    private static AuthDataEntity getAuthDataEntityByLoginAndPassword (Session session, String login_hash, String password_hash) {
        Query query = session.createQuery("from AuthDataEntity where login=:paramName1 AND password =:paramName2");
        query.setParameter("paramName1", login_hash);
        query.setParameter("paramName2", password_hash);
        List list = query.list();
        if(!list.isEmpty()) {
            return (AuthDataEntity) list.get(0);
        }
        return null;
    }

    private static AuthDataEntity getLoginID (Session session, String login_hash) {
        Query query = session.createQuery("from AuthDataEntity where login=:paramName1");
        query.setParameter("paramName1", login_hash);
        List list = query.list();
        if(!list.isEmpty()) {
            return (AuthDataEntity) list.get(0);
        }
        return null;
    }

    public static boolean checkAuthData(AuthData authData) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getAuthDataEntityByLoginAndPassword(session, authData.getLogin(), authData.getPassword());
        session.close();
        return authDataEntity!=null;
    }

    public static void createFileRecord(String login, String path, String fileName, String pathNameHash, String children) {
        System.out.println("Запись в БД!");
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session,login);
        System.out.println("Login is: " + authDataEntity.getLogin());
        System.out.println("Файл уже существует в БД?: " + isFileAlreadyExist(session, authDataEntity, pathNameHash));

        if (!isFileAlreadyExist(session, authDataEntity, pathNameHash)) {
            ///
            FilesEntity filesEntity = new FilesEntity();
            filesEntity.setPath(path);
            filesEntity.setFileName(fileName);
            filesEntity.setPathNameHash(pathNameHash);
            filesEntity.setAuthdataById(authDataEntity);
            filesEntity.setChildren(children);
            ///
            session.beginTransaction();
            session.save(filesEntity);
            session.getTransaction().commit();
        } else {
            //здесь будет код на перезапись файла
        }

        session.close();
    }

    public static List<File> fileList (String login, String path) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        System.out.println("Сессия открыта? : "+session.isOpen());
        ///
        AuthDataEntity authDataEntity = getLoginID(session, login);
        System.out.println(authDataEntity.getLogin());
        Query query;

        if (path == null) {
            query = session.createQuery("FROM FilesEntity WHERE authdataById =:paramName1 AND path IS NULL");
        } else {
            query = session.createQuery("FROM FilesEntity WHERE authdataById =:paramName1 AND path =:paramName2");
            query.setParameter("paramName2", path);
        }

        query.setParameter("paramName1", authDataEntity);
        List list = query.list();
        ///
        session.close();
        System.out.println(list);

        return FilesEntityToFile(list);
    }

    private static List<File> FilesEntityToFile(List<FilesEntity> list) {
        List <File> filesList = new ArrayList<>();
        for (FilesEntity filesEntity : list) {
            File file = new File(filesEntity.getPath(),filesEntity.getFileName());
            filesList.add(file);
        }
//        list.stream().map(filesEntity -> new File(filesEntity.getFileName(), filesEntity.getPath())).forEach(filesList::add);
        return filesList;
    }

    private static boolean isFileAlreadyExist(Session session, AuthDataEntity authDataEntity, String pathNameHash) {
        Query query = session.createQuery("from FilesEntity where authdataById =:paramName1 AND pathNameHash =:paramName2");
        query.setParameter("paramName1", authDataEntity);
        query.setParameter("paramName2", pathNameHash);
        return !query.list().isEmpty();

    }
}
