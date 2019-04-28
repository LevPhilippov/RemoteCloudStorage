package com.filippov;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
public class CloudWrappedObject implements Serializable {

    private TypeEnum typeEnum;
    private byte[] bytes;
    private long hashSum;
    private int parcelNumber;
    private int parcelsInObject;
    private String fileName;
    private String localPath;
    private String targetPath;
    private String msg;

    public CloudWrappedObject(TypeEnum typeEnum, byte[] bytes, String fileName, String targetPath, String localPath) {
        this.typeEnum = typeEnum;
        this.bytes = bytes;
        this.fileName = fileName;
        this.localPath = localPath;
        this.targetPath = targetPath;
    }

    public enum TypeEnum {
        FILE, SEPARATEDFILE
    }

    public static CloudWrappedObject wrapFile(Path path, String targetPath, String localPath) throws IOException {
        System.out.println("-----------------------------");
        System.out.printf("Wrapping object %s in directory %s into %s Server Folder: ", path.getFileName().toString(), localPath, targetPath);
        byte[] bytes = null;
        bytes = Files.readAllBytes(path);
        CloudWrappedObject cwo = new CloudWrappedObject(CloudWrappedObject.TypeEnum.FILE, bytes, path.getFileName().toString(), targetPath, localPath);
        System.out.println("Байтов прочитано " + bytes.length);
        return cwo;
    }

    public static void writeIntoChannel(Path path, String targetPath, String localPath, Channel channel) {
        System.out.printf("Файл %s будет записан в канал и размещен в папке %s ", path.getFileName().toString(), targetPath);
        System.out.println();
        try {
            CloudWrappedObject cwo = wrapFile(path, targetPath, localPath);
            channel.writeAndFlush(cwo).addListener((ChannelFutureListener) channelFuture -> {
                System.out.println("Writing Complete!");
            });
        } catch (IOException a) {
            System.out.println("Ошибка записи");
            a.printStackTrace();
        }
    }


}
