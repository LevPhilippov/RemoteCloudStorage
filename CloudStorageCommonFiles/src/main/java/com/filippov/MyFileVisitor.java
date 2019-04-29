package com.filippov;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor implements FileVisitor<Path> {

    Path baseLocalPath;
    Path baseTargetPath;
    io.netty.channel.Channel channel;
    Request.RequestType requestType;

    public MyFileVisitor(File localPath, File baseTargetPath, io.netty.channel.Channel channel, Request.RequestType requestType) {
        this.baseLocalPath = localPath.toPath();
        this.baseTargetPath = baseTargetPath.toPath();
        this.channel = channel;
        this.requestType = requestType;
        System.out.println("Локальный путь " + localPath);
        System.out.println("Целевой путь " + baseTargetPath);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public  FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        switch (requestType){
            case GETFILES:{
                Path targetPath = Paths.get(baseTargetPath.toString() + '/' + baseLocalPath.relativize(file).toString());
                System.out.println("targetPath" + targetPath);
                WrappedFileHandler.parseToSend(file, targetPath, channel);
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
        System.out.println("Ошибка отправки файла");
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        switch (requestType){
            case DELETEFILES: Files.delete(dir);
            break;
            default:break;
        }
        return FileVisitResult.CONTINUE;
    }
}


