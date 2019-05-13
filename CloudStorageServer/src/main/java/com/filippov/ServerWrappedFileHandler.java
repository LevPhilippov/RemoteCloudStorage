package com.filippov;

import com.filippov.HibernateUtils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ServerWrappedFileHandler {

    public static int byteBufferSize = 1024*1024*5;


    public static void parseToSave(WrappedFile wrappedFile) {
        switch (wrappedFile.getTypeEnum()) {
            case FILE: saveFile(wrappedFile); break;
            case CHUNKED: saveChunk(wrappedFile); break;
            default: showError(); break;
        }
    }

    public static void parseToSend(String login, File file, Channel channel, Path startPath) {
        // базовый путь
        Path relativePath;
        // если запрашиваемый файл это папка - запускаем рекурсию пока не доберемся до файлов
        if (Utils.isThatDirectory(login, file)) {
            if (startPath == null) {
                startPath = file.toPath().getParent(); // это можно получить и из БД
                System.out.println("Startpath установлен как: " + startPath);
            }
            System.out.println("Работаем с директорией!");
            List <File> files = Utils.fileList(login, file);
            System.out.println("Список файлов в директории " + file + ": " + files );
            for (File f : files) {
                parseToSend(login, f, channel, startPath);
            }
            return;
        }
        // далее блок работы с файлами
        //если в запросе были папки  - конструируем путь чтобы сохранить файловую структуру сервера у клиента.
            if (startPath != null) {
                relativePath = startPath.relativize(file.toPath());
                System.out.println("RelativePath установлен как: " + relativePath);
            } else {
                 relativePath = Paths.get("root");
            }
        //определяем реальный путь к файлу в хранилище сервера
            Path serverPath = Paths.get(Server.rootPath.toString(), Utils.getRecordedPath(login, file).toString());

            if (!Files.exists(serverPath)) {
                System.out.println("Запрошенный файл не найден!");
                return;
            }

            long fileSize = 0;
            try {
                fileSize = Files.size(serverPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Упс! Файл попал в парсер и неожиданно потерялся!");
            }

            if (fileSize <= byteBufferSize)
                wrapAndWriteFile(serverPath, relativePath, file.getName(), channel);
            else
                wrapAndWriteChunk(serverPath, relativePath, file.getName(), channel);
    }

    public static void wrapAndWriteFile(Path serverPath, Path targetPath, String fileName, Channel channel) {
        System.out.printf("Файл %s будет записан в канал и размещен в папке %s ", fileName, targetPath.toString());
        System.out.println();
        try {
            byte[] bytes = null;
            bytes = Files.readAllBytes(serverPath);

            WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.FILE, bytes,
                    1,1,
                    fileName, targetPath.toFile());

            channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                System.out.println("Writing Complete!");
            });
        } catch (IOException a) {
            System.out.println("Ошибка записи");
            a.printStackTrace();
        }
    }

    public static void wrapAndWriteChunk(Path serverPath, Path targetPath, String fileName, Channel channel) {
        System.out.println("-------------------------------------------------------------");
        System.out.println("Указанный путь к файлу: " + serverPath.toString());
        System.out.println("Имя файла: " + fileName);
        System.out.println("Указанный удаленный путь" + targetPath.toString());

        if(Files.exists(serverPath)) {
            long chunkCounter = 1;
            long chunks=0;
            try {
                if(Files.size(serverPath)%byteBufferSize == 0){
                    chunks = Files.size(serverPath)/byteBufferSize;
                }
                else {
                    chunks = Math.round(Files.size(serverPath)/byteBufferSize)+1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] byteBuffer = new byte[byteBufferSize];
            int bytesRed=0;
            File file = serverPath.toFile();
            try(FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
                while ((bytesRed=bis.read(byteBuffer))>0) {

                    WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.CHUNKED,
                            byteBuffer, chunkCounter, chunks,
                            fileName, targetPath.toFile());

                    channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                        System.out.println("Writing Complete!");
                    }).sync();
                    chunkCounter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveFile(WrappedFile wrappedFile) {
        String hash_file_name = DigestUtils.md5Hex(wrappedFile.getTargetPath().getPath() + wrappedFile.getFileName());
        Path path = Paths.get(Server.rootPath.toString(), wrappedFile.getLogin(), hash_file_name);
        System.out.println("Файл будет записан по адресу: " + path.toString());
        try {
            if(Files.exists(path)){
                Files.delete(path);
            }
//                Files.createDirectories(path.getParent());
            Files.write(path, wrappedFile.getBytes());
            System.out.println("Обращение к БД!.....................................");
            Utils.createFileRecord(wrappedFile.getLogin(), wrappedFile.getTargetPath().getPath(), wrappedFile.getFileName(), hash_file_name, null);
        } catch (IOException e) {
            System.out.println("Не удалось записать файл!");
            e.printStackTrace();
        }
    }

    private static void saveChunk(WrappedFile wrappedFile) {
        System.out.println("Запись чанка № " + wrappedFile.getChunkNumber() + " из " + wrappedFile.getChunkslsInFile());
        String hash_file_name = DigestUtils.md5Hex(wrappedFile.getTargetPath().getPath() + wrappedFile.getFileName());
        Path path = Paths.get(Server.rootPath.toString(), wrappedFile.getLogin(), hash_file_name);
            try {
//                если файла по этому адресу еще не существует
                if(!Files.exists(path)) {
                    System.out.println("Записей не обнаружено! Создаю директории и пишу первый чанк!");
                    Files.createFile(path);
                    Files.write(path,wrappedFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    return;
                } else if (Files.exists(path) && wrappedFile.getChunkNumber()==1) {
                    //если запись существует, и передается чанк с номером 1 - значит файл необходимо перезаписать
                    System.out.println("Запись обнаружена при этом записывается первый чанк! Удаляю старый файл и записываю первый чанк!");
                    Files.delete(path);
                    Files.write(path,wrappedFile.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                }
                //если это последний чанк файла вносим запись в базу данных
                if(wrappedFile.getChunkNumber() == wrappedFile.getChunkslsInFile()) {
                    System.out.println("Запись обнаружена, пишется последний чанк " + wrappedFile.getChunkNumber() + " Всего чанков: " + wrappedFile.getChunkslsInFile());
                    Files.write(path,wrappedFile.getBytes(), StandardOpenOption.APPEND);
                    System.out.println("Обращение к БД!.....................................");
                    Utils.createFileRecord(wrappedFile.getLogin(), wrappedFile.getTargetPath().getPath(), wrappedFile.getFileName(), hash_file_name, null);
                    return;
                }
                System.out.printf("Запись обнаружена, пишется чанк %d из %d \n ", wrappedFile.getChunkNumber(), wrappedFile.getChunkslsInFile());
                //если ни то, ни другое
                Files.write(path,wrappedFile.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                e.printStackTrace();
            }
    }

    private static void showError() {
        System.out.println("Неизвестная команда или данные повреждены");
    }




}
