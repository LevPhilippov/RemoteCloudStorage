package com.filippov.HibernateUtils;

import com.filippov.AuthData;
import com.filippov.Factory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Utils {

    private static final Logger LOGGER = LogManager.getLogger(Utils.class.getCanonicalName());

    public static boolean writeNewClientAuthData(AuthData authData) {
        LOGGER.info("Writing new AuthData {}", authData.getLogin());
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        if (getAuthDataEntityByLoginAndPassword(session, authData.getLogin(), authData.getPassword())!=null) {
            return false;
        }
        session.beginTransaction();
        ///
        AuthDataEntity authDataEntity = new AuthDataEntity();
        authDataEntity.setLogin(authData.getLogin());
        authDataEntity.setPassword(authData.getPassword());
        ///
        session.save(authDataEntity);
        session.getTransaction().commit();
        session.close();
        return true;
    }

    private static AuthDataEntity getAuthDataEntityByLoginAndPassword (Session session, String login_hash, String password_hash) {
        LOGGER.debug("Requesting AuthDataEntity by login {} and password {}", login_hash, password_hash);
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
        LOGGER.debug("Getting loginID {}", login_hash);
        Query query = session.createQuery("from AuthDataEntity where login=:paramName1");
        query.setParameter("paramName1", login_hash);
        List list = query.list();
        if(!list.isEmpty()) {
            return (AuthDataEntity) list.get(0);
        }
        return null;
    }

    public static boolean checkAuthData(AuthData authData) {
        LOGGER.debug("Checking presence of AuthData {}, {}", authData.getLogin(), authData.getPassword());
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getAuthDataEntityByLoginAndPassword(session, authData.getLogin(), authData.getPassword());
        session.close();
        return authDataEntity!=null;
    }

    public static void createFileRecord(String login, String path, String fileName, String pathNameHash, String children) {
        LOGGER.info("Запись в БД! Login: {}\n Path: {}\n FileName: {}", login, path, fileName);
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session,login);

        if (!isFileAlreadyExist(session, authDataEntity, pathNameHash)) {
            Transaction tx = session.beginTransaction();
            ///
            FilesEntity filesEntity = new FilesEntity();
            filesEntity.setId(authDataEntity.getId());
            filesEntity.setPath(path);
            filesEntity.setFileName(fileName);
            filesEntity.setPathNameHash(pathNameHash);
            filesEntity.setAuthdataById(authDataEntity);
            filesEntity.setChildren(children);
            ///
            session.save(filesEntity);
            tx.commit();
        } else {
            LOGGER.info("Запись уже существует!");
        }
        session.close();
    }

    public static boolean deleteFileRecord(String login, File file) {
        LOGGER.info("Удаление из БД. Login: {}\n File: {}", login, file.getPath());
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session,login);
        Transaction tx = session.beginTransaction();
        Query query =  session.createQuery("DELETE FilesEntity WHERE id =:paramName1 AND pathNameHash =: paramName2");
        query.setParameter("paramName1", authDataEntity.getId());
        query.setParameter("paramName2", Factory.MD5PathNameHash(file.getParent(), file.getName()));
        query.executeUpdate();
        tx.commit();
        session.close();
        return true;
    }

    public static boolean isThatDirectory(String login, File file) {
        //если запрошен корень - просто возвращаем true
        if(file.getName().equals("root")) {
            return true;
        }
        FilesEntity filesEntity = getFileRecord(login, file);
        return filesEntity.getChildren()!=null;
    }

    public static List<File> fileList (String login, File path) {
        List <FilesEntity> filesEntityList = filesEntityList(login, path);
        List <File> filesList = new ArrayList<>();
        filesEntityList.stream().map(filesEntity -> new File(filesEntity.getPath(),filesEntity.getFileName())).forEach(filesList::add);
        return filesList;
    }

    private static List<FilesEntity> filesEntityList (String login, File path) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session, login);
        Query query = session.createQuery("FROM FilesEntity WHERE id =:paramName1 AND path =:paramName2");
        query.setParameter("paramName2", path.getPath());
        query.setParameter("paramName1", authDataEntity.getId());
        List list = query.list();
        session.close();
        return list;
    }

    private static boolean isFileAlreadyExist(Session session, AuthDataEntity authDataEntity, String pathNameHash) {
        Query query = session.createQuery("from FilesEntity where id =:paramName1 AND pathNameHash =:paramName2");
        query.setParameter("paramName1", authDataEntity.getId());
        query.setParameter("paramName2", pathNameHash);
        return !query.list().isEmpty();
    }

    public static FilesEntity getFileRecord(String login, File path) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session, login);
        Query query = session.createQuery("FROM FilesEntity WHERE id =:paramName1 AND path_name_hash =:paramName2");
        query.setParameter("paramName1", authDataEntity.getId());
        query.setParameter("paramName2", Factory.MD5PathNameHash(path.getParent(), path.getName()));
        List <FilesEntity> list = query.list();
        if (list.size()==1) {
            return list.get(0);
        }
        return null;
    }

    public static Path getRecordedPath(String login, File path) {
        FilesEntity filesEntity = getFileRecord(login, path);
        return Paths.get(login, filesEntity.getPathNameHash());
    }



}