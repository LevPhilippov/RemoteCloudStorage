package com.filippov;

import com.filippov.HibernateUtils.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class ServerWrappedFileHandler {

    public static int byteBufferSize = 1024*1024*5;
    private static final Logger LOGGER = LogManager.getLogger(ServerWrappedFileHandler.class.getCanonicalName());
    public static void parseToSave(WrappedFile wrappedFile) {
        switch (wrappedFile.getTypeEnum()) {
            case FILE: saveFile(wrappedFile); break;
            case CHUNKED: saveChunk(wrappedFile); break;
            default: showError(); break;
        }
    }

    public static void parseToSend(String login, File file, Channel channel, Path startPath) {
        LOGGER.debug("Зашли в метод отправки файлов: {}", file.getPath());
        // базовый путь
        Path relativePath;
        // если запрашиваемый файл это папка - запускаем рекурсию пока не доберемся до файлов
        if (Utils.isThatDirectory(login, file)) {
            LOGGER.debug("Запрошенный файл является директорией: {}", file.getPath());
            if (startPath == null) {
                startPath = file.toPath().getParent(); // это можно получить и из БД
                LOGGER.trace("Startpath установлен как: {} ", startPath);
            }
            List <File> files = Utils.fileList(login, file);
            LOGGER.trace("Список файлов в директории {} : {}", file.getPath(),  files );
            for (File f : files) {
                parseToSend(login, f, channel, startPath);
            }
            return;
        }
        // далее блок работы с файлами
        //если в запросе были папки  - конструируем путь чтобы сохранить файловую структуру сервера у клиента.
            if (startPath != null) {
                relativePath = startPath.relativize(file.toPath());
                LOGGER.trace("Работа с файлами. RelativePath установлен как: {} ", relativePath.toString());
            } else {
                 relativePath = Paths.get("root");
            }
        //определяем реальный путь к файлу в хранилище сервера
            Path serverPath = Paths.get(Server.rootPath.toString(), Utils.getRecordedPath(login, file).toString());

            if (!Files.exists(serverPath)) {
                LOGGER.warn("Запрошенный файл не найден в хранилище {}", serverPath.toString());
                return;
            }

            long fileSize = 0;
            try {
                fileSize = Files.size(serverPath);
            } catch (IOException e) {
                LOGGER.error("Упс! Файл попал в парсер и неожиданно потерялся!\n" + e.getMessage());
            }

            if (fileSize <= byteBufferSize)
                wrapAndWriteFile(serverPath, relativePath, file.getName(), channel);
            else
                wrapAndWriteChunk(serverPath, relativePath, file.getName(), channel);
    }

    public static void wrapAndWriteFile(Path localPath, Path targetPath, String fileName, Channel channel) {
        LOGGER.debug("Зашли в метод отправки файлов.\nФайл {} будет записан в канал и размещен в папке {}\n", fileName, targetPath.toString());
        try {
            byte[] bytes = Files.readAllBytes(localPath);

            WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.FILE, bytes,
                    1,1,
                    fileName, targetPath.toFile());
            wrappedFile.setMD5Hash(Factory.MD5FileHash(localPath));
            channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                if(channelFuture.isSuccess()){
                    LOGGER.trace("Writing Complete!");
                }
            });
        } catch (IOException a) {
            LOGGER.error("Ошибка записи!\n" + a.getMessage() );
        }
    }

    public static void wrapAndWriteChunk(Path localPath, Path targetPath, String fileName, Channel channel) {
        LOGGER.debug("Зашли в метод отправки чанк-файлов!\nУказанный путь к файлу: {}\nИмя файла: {}\nУказанный удаленный путь: {}",localPath.toString(),fileName,targetPath.toString());
        if(Files.exists(localPath)) {
            long chunkCounter = 1;
            long chunks=0;
            try {
                if(Files.size(localPath)%byteBufferSize == 0){
                    chunks = Files.size(localPath)/byteBufferSize;
                }
                else {
                    chunks = (Files.size(localPath)/byteBufferSize)+1;
                }
            } catch (IOException e) {
                LOGGER.error("Ошибка записи чанков!" + e.getMessage());
            }

            byte[] byteBuffer = new byte[byteBufferSize];
            int bytesRed=0;
            File file = localPath.toFile();
            try(FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
                while ((bytesRed=bis.read(byteBuffer))>0) {
                    WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.CHUNKED,
                            byteBuffer, chunkCounter, chunks,
                            fileName, targetPath.toFile());
                    //lля последнего чанка необходимо обрезать байтовый массив во избежания появления нулевых байтов
                    if(chunkCounter==chunks){
                        byteBuffer = Arrays.copyOfRange(byteBuffer,0,bytesRed);
                        LOGGER.warn("Чанк номер {} из {}\nВычитано байтов: {}\nРазмер последнего байтового массива: {}"
                                ,chunkCounter, chunks,byteBuffer.length, bytesRed);
                        wrappedFile.setBytes(byteBuffer);
                        wrappedFile.setMD5Hash(Factory.MD5FileHash(localPath));
                    }
                    ///т.к. в анонимном классе Java8 хочет effectively final - переменные
                    long finalChunkCounter = chunkCounter;
                    long finalChunks = chunks;
                    ///
                    channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                        if(channelFuture.isSuccess()){
                            LOGGER.info("Writing complete! Чанк {} из {}", finalChunkCounter, finalChunks);
                        }
                    });
                    chunkCounter++;
                }
            } catch (IOException e) {
                LOGGER.error("Ошибка записи! Чанк {} из {}\n", chunkCounter, chunks);
                LOGGER.error(e.getMessage());
            }
        }
    }

    private static void saveFile(WrappedFile wrappedFile) {
        LOGGER.debug("Зашли в метод сохранения файлов!");
        String hash_file_name = Factory.MD5PathNameHash(wrappedFile.getTargetPath().getPath(),wrappedFile.getFileName());
        Path path = Paths.get(Server.rootPath.toString(), wrappedFile.getLogin(), hash_file_name);
        LOGGER.debug("Файл будет записан по адресу: {}", path.toString());
        try {
            if(Files.exists(path)){
                Files.delete(path);
            }
            Files.write(path, wrappedFile.getBytes());
            LOGGER.error("MD5 comparision: " + wrappedFile.getMD5Hash().equals(Factory.MD5FileHash(path)));
            Utils.createFileRecord(wrappedFile.getLogin(), wrappedFile.getTargetPath().getPath(), wrappedFile.getFileName(), hash_file_name, null);
        } catch (IOException e) {
            LOGGER.error("He удалось сохранить файл!\n" + e.getMessage() );
        }
    }

    private static void saveChunk(WrappedFile wrappedFile) {
        LOGGER.debug("Запись чанка № {} из {} ", wrappedFile.getChunkNumber(), wrappedFile.getChunkslsInFile());
        String hash_file_name = Factory.MD5PathNameHash(wrappedFile.getTargetPath().getPath(),wrappedFile.getFileName());
        Path path = Paths.get(Server.rootPath.toString(), wrappedFile.getLogin(), hash_file_name);
            try {
                if(!Files.exists(path)) {
                    //если файла по этому адресу еще не существует
                    LOGGER.info("Записей не обнаружено! Создаю директории и пишу первый чанк!");
                    Files.createFile(path);
                    Files.write(path,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                    return;
                } else if (Files.exists(path) && wrappedFile.getChunkNumber()==1) {
                    //если запись существует, и передается чанк с номером 1 - значит файл необходимо перезаписать
                    LOGGER.info("Запись обнаружена при этом записывается первый чанк! Удаляю старый файл и записываю первый чанк!");
                    Files.delete(path);
                    Files.createFile(path);
                    Files.write(path, wrappedFile.getBytes(), StandardOpenOption.WRITE);
                    return;
                    }
                //если ни то, ни другое
                    LOGGER.info("Запись обнаружена, пишется чанк {} из {} ", wrappedFile.getChunkNumber(), wrappedFile.getChunkslsInFile());
                    Files.write(path, wrappedFile.getBytes(), StandardOpenOption.APPEND);
                //если это последний чанк файла вносим запись в базу данных
                if(wrappedFile.getChunkNumber() == wrappedFile.getChunkslsInFile()) {
                    LOGGER.error("MD5 comparision: " + wrappedFile.getMD5Hash().equals(Factory.MD5FileHash(path)));
                    Utils.createFileRecord(wrappedFile.getLogin(), wrappedFile.getTargetPath().getPath(), wrappedFile.getFileName(), hash_file_name, null);
                    }
            } catch (IOException e) {
                LOGGER.error("Ошибка записи чанка {}!\n", wrappedFile.getChunkNumber());
                LOGGER.error(e.getMessage());
            }
    }
    private static void showError() {
        LOGGER.error("Неизвестная команда или данные повреждены!");
    }




}
