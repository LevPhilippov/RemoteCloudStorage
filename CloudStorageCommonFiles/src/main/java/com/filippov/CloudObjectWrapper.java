package com.filippov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudObjectWrapper {

    public static CloudWrappedObject wrapFile(Path path, String localPath, String targetPath) throws IOException {
        System.out.println("-----------------------------");
        System.out.println("Wrapping: " + path.toString());
        System.out.println("Destnation is " + targetPath);

        byte[] bytes = null;
        if (Files.exists(path)) {
                 bytes = Files.readAllBytes(path);
        }
        CloudWrappedObject cwo = new CloudWrappedObject(CloudWrappedObject.TypeEnum.FILE, bytes, path.getFileName().toString(), localPath, targetPath);
        System.out.println("Байтов прочитано " + bytes.length);
        return cwo;
    }



}
