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
        ///
        if(targetPath.toString().equals("root")) {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getFileName());
            System.out.println("Файл будет записан по адресу: " + targetPath);
        } else {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getTargetPath().toString());
        }

        try {
            //если такого файла не существует
            if(!Files.exists(targetPath)){
                Files.createDirectories(targetPath.getParent());
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                return;
            } //если файл уже существует он будет перезаписан
            else if (Files.exists(targetPath) && wrappedFile.getChunkNumber()==1) {
                Files.delete(targetPath);
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.WRITE);
                return;
            }
            //если ни то ни другое
            Files.write(targetPath,wrappedFile.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Не удалось записать файл!");
            e.printStackTrace();
        } finally {
            Network.getInstance().getController().refreshLocalFilesList();
        }
    }

    private static void showError() {
        System.out.println("Неизвестная команда");
    }


    private static void saveFile(WrappedFile wrappedFile) {
        Path targetPath = wrappedFile.getTargetPath().toPath();
        ///
        if(targetPath.toString().equals("root")) {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getFileName());
            System.out.println("Файл будет записан по адресу: " + targetPath);
        } else {
            targetPath = Paths.get(PathHolder.baseLocalPath.toString(),
                    Network.getInstance().getPathHolder().getClientPath().toString(),
                    wrappedFile.getTargetPath().toString());
        }
        ///
        if(!Files.exists(targetPath)){
            try {
                Files.createDirectories(targetPath.getParent());
                Files.createFile(targetPath);
                Files.write(targetPath,wrappedFile.getBytes());
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл уже существует");
        }
        Network.getInstance().getController().refreshLocalFilesList();
    }


    public static void wrapAndWriteFile(Path localPath, Path serverPath, Channel channel) {
        System.out.printf("Файл %s будет записан в канал и размещен в папке %s\n ", localPath.getFileName(), serverPath);
        try {
            byte[] bytes = Files.readAllBytes(localPath);
            WrappedFile wrappedFile = new WrappedFile(WrappedFile.TypeEnum.FILE, bytes,
                    1,1,
                    localPath.getFileName().toString(),serverPath.toFile());
//            if (serverPath!=null)
//                wrappedFile.setTargetPath(serverPath.toFile());
            System.out.println("RelativePath у собранного файла: " + wrappedFile.getTargetPath());
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
        System.out.println("Указанный путь к файлу: " + localPath);
        System.out.println("Имя файла: " + localPath.getFileName());
        System.out.println("Указанный удаленный путь" + targetPath);

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

                    channel.writeAndFlush(wrappedFile).addListener((ChannelFutureListener) channelFuture -> {
                        System.out.println("Writing Complete!");
                    }).sync();
                    System.out.printf("Записан чанк %d из %d\n", chunkCounter, chunks);
                    chunkCounter++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
