package com.filippov;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClientWrappedFileHandler{

    public static int byteBufferSize = 1024*1024;

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
        Path targetPath = getlocalPath(wrappedFile);

        try {
            //если такого файла не существует
            if(!Files.exists(targetPath)){
                Files.createDirectories(targetPath.getParent());
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                Controller.controller.setPullProgress(targetPath.getFileName().toString(), 0L);

                return;
            } //если файл уже существует он будет перезаписан
            else if (Files.exists(targetPath) && wrappedFile.getChunkNumber()==1) {
                Files.delete(targetPath);
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                Controller.controller.setPullProgress(targetPath.getFileName().toString(), 0L);
                return;
            }
            //если ни то ни другое
            Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.APPEND);
            Controller.controller.setPullProgress(targetPath.getFileName().toString(), wrappedFile.getChunkNumber()*100/wrappedFile.getChunkslsInFile());
        } catch (IOException e) {
            Network.messageService.setSingleServiseMessage("Не удалось записать файл!");
            System.out.println("Не удалось записать файл!");
            e.printStackTrace();
        } finally {
            Controller.controller.refreshLocalFilesList();
        }
    }

    private static void showError() {
        System.out.println("Неизвестная команда");
    }


    private static void saveFile(WrappedFile wrappedFile) {
        Path targetPath = getlocalPath(wrappedFile);
        ///
        if(!Files.exists(targetPath)){
            try {
                Files.createDirectories(targetPath.getParent());
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes());
                Controller.controller.setPullProgress(targetPath.getFileName().toString(), 0L);
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                Network.messageService.setSingleServiseMessage("Не удалось записать файл!");
                e.printStackTrace();
            }
        } else {
            try {
                Network.messageService.setSingleServiseMessage("Файл c таким именем уже существует! Выполняется перезапись!");
                System.out.println("Файл уже существует! Перезаписываю!");
                Files.delete(targetPath);
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                Controller.controller.setPullProgress(targetPath.getFileName().toString(), 0L);
            } catch (IOException e) {
                e.printStackTrace();
                Network.messageService.setSingleServiseMessage("Ошибка! Не удалось записать файл!");
            }
        }
        Controller.controller.refreshLocalFilesList();
    }

    private static Path getlocalPath(WrappedFile wrappedFile) {
        Path targetPath = wrappedFile.getTargetPath().toPath();
        ///
        if (targetPath.toString().equals("root")) {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getFileName());
            System.out.println("Файл будет записан по адресу: " + targetPath);
        } else {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getTargetPath().toString());
        }
        return targetPath;
    }


    public static void wrapAndWriteFile(Path localPath, Path serverPath, Channel channel) {
        System.out.printf("Файл %s будет записан в канал и размещен в папке %s\n ", localPath.getFileName(), serverPath);
        try {
            byte[] bytes = Files.readAllBytes(localPath);
            WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.FILE, bytes,
                    1,1,
                    localPath.getFileName().toString(),serverPath.toFile());
            System.out.println("RelativePath у собранного файла: " + wrappedFile.getTargetPath());
            channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                if(channelFuture.isSuccess()){
                    //уведомление о прогрессе отправки летит в сервисную область.
                    Controller.controller.setPushProgress(localPath.getFileName().toString(), 100L);
                }
            });
        } catch (IOException a) {
            Network.messageService.setSingleServiseMessage("Не удалось выполнить запись файла в канал!");
            System.out.println("Ошибка записи");
            a.printStackTrace();
        }
    }


    public static void wrapAndWriteChunk(Path localPath, Path targetPath, Channel channel) {
        System.out.println("-------------------------------------------------------------");
        System.out.println("Указанный путь к файлу: " + localPath);
        System.out.println("Имя файла: " + localPath.getFileName());
        System.out.println("Указанный удаленный путь" + targetPath);
        Network.messageService.setSingleServiseMessage("Записываем файл: " + localPath.getFileName());

        if(Files.exists(localPath)) {
            long chunkCounter = 1;
            long chunks=0;
            try {
                if(Files.size(localPath)%byteBufferSize == 0){
                    chunks = Files.size(localPath)/byteBufferSize;
                }
                else {
                    chunks = Math.round(Files.size(localPath)/byteBufferSize)+1;
                }
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
                    ///т.к. в анонимном классе Java8 хочет effectively final - переменные
                    long finalChunkCounter = chunkCounter;
                    long finalChunks = chunks;
                    ///
                    Controller.controller.setPushProgress(file.getName(), 0L);
                    channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                        if(channelFuture.isSuccess()){
                            System.out.printf("Записан чанк %d из %d\n", finalChunkCounter, finalChunks);
                            //уведомление о прогрессе отправки летит в сервисную область.
                            Controller.controller.setPushProgress(file.getName(), finalChunkCounter*100/finalChunks);
                        }
                    });
                    chunkCounter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Network.messageService.setSingleServiseMessage("Не удалось выполнить запись файла в канал!");
            }
        }
    }
}
