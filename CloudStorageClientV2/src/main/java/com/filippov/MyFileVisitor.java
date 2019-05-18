package com.filippov;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor implements FileVisitor<Path> {

    io.netty.channel.Channel channel;
    Request.RequestType requestType;
    Path startPath = null;
    Path relativePath=Network.getInstance().getPathHolder().getServerPath();

    /**
     *Конструктор принимает на вход:
     * @param requestType команда на обработку файла
     * @param channel канал передачи данных (при необходимости)*/
    public MyFileVisitor(io.netty.channel.Channel channel, Request.RequestType requestType) {
        this.channel = channel;
        this.requestType = requestType;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
       //домашний путь вначале обхода
        if(startPath == null) {
            startPath = dir.getParent();
//            System.out.println("STARTPATH IS: " + startPath);
        }

        relativePath = Paths.get (Network.getInstance().getPathHolder().getServerPath().toString(), startPath.relativize(dir).toString());
//        System.out.println("-----------");
//        System.out.println("Previzit: " + dir);
//        System.out.println("Relativize: " + relativePath);
//        System.out.println("-----------");

        switch (requestType){
            case SENDFILES:{
//                System.out.println("Отправляю запрос на создание папки " + dir.getFileName() + " по адресу" + relativePath);
                Request createFolder = new Request();
                createFolder.setRequestType(Request.RequestType.CREATE_FOLDER).setServerPath(relativePath.toFile());
                channel.writeAndFlush(createFolder);
                break;
            }
            default: break;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public  FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//        System.out.printf("File %s with path %s\n", file.getFileName(), relativePath);
        switch (requestType){
            case SENDFILES:{
                ClientWrappedFileHandler.parseToSend(file, relativePath, channel);
                break;
            }
            case DELETEFILES:{
                Files.delete(file);
                break;
            }
            default: break;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//        System.out.println("Ошибка отправки файла");
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        switch (requestType){
            case DELETEFILES: Files.delete(dir); break;
            default: break;
        }
        return FileVisitResult.CONTINUE;
    }
}


