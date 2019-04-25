package com.filippov;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudObjectWrapper {

    public static CloudWrappedObject wrapFile(Path path, String fileName) throws IOException {
        System.out.println("Wrapping: " + path.toString());
        System.out.println("FileName is " + fileName);
//        System.out.println("DestPath is " + destPath.toString());

        byte[] bytes = null;
        if (Files.exists(path)) {
                 bytes = Files.readAllBytes(path);
        }
        CloudWrappedObject cwo = new CloudWrappedObject(CloudWrappedObject.TypeEnum.FILE, bytes, fileName);
//        cwo.setPathAtTargetRepository(destPath);
        System.out.println("Байтов прочитано " + bytes.length);
        return cwo;
    }



}
