package com.filippov.HibernateUtils;

import com.filippov.AuthData;
import com.filippov.Request;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static List<FilesEntity> filesEntityList (String login, File path) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session, login);
        Query query = session.createQuery("FROM FilesEntity WHERE authdataById =:paramName1 AND path =:paramName2");
        System.out.println(path.getPath());
        query.setParameter("paramName2", path.getPath());
        query.setParameter("paramName1", authDataEntity);
        List list = query.list();
//
//        if(list.isEmpty()) {
//            isThatDirectory(session, authDataEntity, path);
//            session.close();
//            return null;
//        }
        session.close();
        return list;
    }

    public static List<File> fileList (String login, File path) {
        List <FilesEntity> filesList = filesEntityList(login, path);
//        if(filesList==null) {
//            System.out.println("Запрошенный адрес является файлом!");
//            return null;
//        }
        return FilesEntityToFile(filesList);
    }

//    private static boolean isThatDirectory(Session session, AuthDataEntity authDataEntity, File path) {
//        //переделать под хэшнамепас
//        Query query = session.createQuery("FROM FilesEntity WHERE authdataById =:paramName1 " +
//                                            "AND path =:paramName2 " +
//                                            "AND file_name =:paramName3 " +
//                                            "AND children IS NULL");
//        query.setParameter("paramName3", path.getName());
//        query.setParameter("paramName2", path.getParent());
//        query.setParameter("paramName1", authDataEntity);
//        List list = query.list();
//        return list.isEmpty();
//    }

    public static boolean isThatDirectory(String login, File file) {
        //если запрошен корень - просто возвращаем true
        if(file.getName().equals("root")) {
            return true;
        }
        FilesEntity filesEntity = isRecordExist(login, file);
        return filesEntity.getChildren()!=null;
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

    private static FilesEntity isRecordExist(String login, File path) {
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session, login);
        Query query = session.createQuery("FROM FilesEntity WHERE authdataById =:paramName1 AND path_name_hash =:paramName2");
        query.setParameter("paramName1", authDataEntity);
        System.out.println(path);
        query.setParameter("paramName2", DigestUtils.md5Hex(path.getParent()+ path.getName()));
        List <FilesEntity> list = query.list();
        if (list.size()==1) {
            return list.get(0);
        }
        return null;
    }

    public static Path getRecordPath(String login, File path) {
        FilesEntity filesEntity = isRecordExist(login, path);
        return Paths.get(login, filesEntity.getPathNameHash());
    }



//    public static Path getServerPath() {
//
//    }

    public static boolean deleteFileRecord(String login, File file) {
        System.out.println("Удаление из БД!");
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        AuthDataEntity authDataEntity = getLoginID(session,login);
        System.out.println("Login is: " + authDataEntity.getLogin());
        Transaction tx = session.beginTransaction();
                    //УДАЛИТЬ ИЗ ПАПКИ!
        System.out.println("Удаляю файл: " + file);
        Query query =  session.createQuery("DELETE FilesEntity WHERE authdataById =:paramName1 AND pathNameHash =: paramName2");
        query.setParameter("paramName1", authDataEntity);
        query.setParameter("paramName2", DigestUtils.md5Hex(file.getParent() + file.getName()));
        System.out.println("Операция выполнено: " + query.executeUpdate());
        tx.commit();
        session.close();
        return true;
    }
}
