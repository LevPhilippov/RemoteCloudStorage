package com.filippov;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;

public class WrappedFileHandler {

    public static int byteBufferSize = 1024*1024*5;


    public static void parseToSave(WrappedFile wrappedFile) {
        switch (wrappedFile.getTypeEnum()) {
            case FILE: saveFile(wrappedFile); break;
            case CHUNKED: saveChunk(wrappedFile); break;
            default: showError(); break;
        }
    }

    public static void parseToSend(Path localPath, Path targetPath, Channel channel) {
        long fileSize=0;
        try {
            fileSize = Files.size(localPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Упс! Файл попал в парсер и неожиданно потерялся!");
        }

        if(fileSize<= byteBufferSize)
            wrapAndWriteFile(localPath, targetPath, channel);
        else
            wrapAndWriteChunk(localPath,targetPath, channel);

    }

    private static void saveChunk(WrappedFile wrappedFile) {
        System.out.println("Запись чанка");
        Path targetPath = wrappedFile.getTargetPath().toPath();
        Path path = Paths.get(targetPath.toString() + "/" + wrappedFile.getFileName());

        if(!Files.exists(path)){
            try {
                Files.createDirectories(targetPath);
                Files.write(path,wrappedFile.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл уже существует");
        }
    }

    private static void showError() {
        System.out.println("Неизвестная команда или данные повреждены");
    }


    private static void saveFile(WrappedFile wrappedFile) {
        Path targetPath = wrappedFile.getTargetPath().toPath();
        Path path = Paths.get(targetPath.toString() + "/" + wrappedFile.getFileName());

        if(!Files.exists(path)){
            try {
                Files.createDirectories(targetPath);
                Files.createFile(path);
                Files.write(path,wrappedFile.getBytes());
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл уже существует");
        }
    }


    public static void wrapAndWriteFile(Path localPath, Path targetPath, Channel channel) {
        System.out.printf("Файл %s будет записан в канал и размещен в папке %s ", localPath.getFileName().toString(), targetPath.toString());
        System.out.println();
        try {
            byte[] bytes = null;
            bytes = Files.readAllBytes(localPath);
//            System.out.println("Байтов прочитано " + bytes.length);

            WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.FILE, bytes,
                    1,1,
                    localPath.getFileName().toString(), targetPath.toFile());

//            WrappedFile wrappedFile = wrapFile(baseLocalPath, baseTargetPath);
            channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                System.out.println("Writing Complete!");
            });
        } catch (IOException a) {
            System.out.println("Ошибка записи");
            a.printStackTrace();
        }
    }


    public static void wrapAndWriteChunk(Path localPath, Path targetPath, Channel channel) {
        System.out.println("-------------------------------------------------------------");
        System.out.println("Указанный путь к файлу: " + localPath.toString());
        System.out.println("Имя файла: " + localPath.getFileName().toString());
        System.out.println("Указанный удаленный путь" + targetPath.toString());

        if(Files.exists(localPath)) {
            long chunkCounter = 1;
            long chunks=0;
            try {
                chunks = Math.round(Files.size(localPath)/byteBufferSize)+1;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] byteBuffer = new byte[byteBufferSize];
            int bytesRed=0;
            File file = localPath.toFile();
            try(FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
                while ((bytesRed=bis.read(byteBuffer))>0) {

                    WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.CHUNKED,
                            byteBuffer, chunkCounter, chunks,
                            localPath.getFileName().toString(), targetPath.toFile());

                    channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                        System.out.println("Writing Complete!");
                    });
                    chunkCounter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
