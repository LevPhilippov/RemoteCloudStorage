package com.filippov;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class MyFileVisitor implements FileVisitor<Path> {

    String localPath;
    String targetPath;
    io.netty.channel.Channel channel;

    public MyFileVisitor(String localPath, String targetPath, io.netty.channel.Channel channel) {
        this.localPath = localPath;
        this.targetPath = targetPath;
        this.channel = channel;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        localPath = dir.toString();
        targetPath = targetPath + "/" + dir.getFileName().toString();
        System.out.println("Зашли в local- директорию : " + localPath);
        System.out.println("Зашли в target-директорию : " + targetPath);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public  FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        CloudWrappedObject.writeIntoChannel(file, targetPath, localPath, channel);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("Ошибка отправки файла");
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        localPath = dir.getParent().toString();
        targetPath = Paths.get(targetPath).getParent().toString();
        System.out.println("Вышли из local-директории сервера: " + localPath);
        System.out.println("Вышли из target-дериктории клиента: " + targetPath);
        return FileVisitResult.CONTINUE;
    }
}


