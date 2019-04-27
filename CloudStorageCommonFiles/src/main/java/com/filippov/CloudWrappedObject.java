package com.filippov;

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

    public CloudWrappedObject(TypeEnum typeEnum, byte[] bytes, String fileName, String localPath, String targetPath) {
        this.typeEnum = typeEnum;
        this.bytes = bytes;
        this.fileName = fileName;
        this.localPath = localPath;
        this.targetPath = targetPath;
    }

    public enum TypeEnum {
        FILE, SEPARATEDFILE
    }

    public static CloudWrappedObject wrapFile(Path path, String localPath, String targetPath) throws IOException {
        System.out.println("-----------------------------");
        System.out.printf("Wrapping object %s in directory %s into %s Server Folder: ", path.getFileName().toString(), localPath, targetPath);

        byte[] bytes = null;
//        if (Files.exists(path)) {
        bytes = Files.readAllBytes(path);
//        }
        CloudWrappedObject cwo = new CloudWrappedObject(CloudWrappedObject.TypeEnum.FILE, bytes, path.getFileName().toString(), localPath, targetPath);
        System.out.println("Байтов прочитано " + bytes.length);
        return cwo;
    }

}
