package com.filippov;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CWOParser {

    public static void parselResolver(CloudWrappedObject cwo) {
        switch (cwo.getTypeEnum()) {
            case FILE: resolveFile(cwo); break;
            case SEPARATEDFILE: resolveSeparatedFile(cwo); break;
            default: resolveError(); break;
        }
    }

    private static void resolveError() {
        System.out.println("Неизвестная команда или данные повреждены");
    }

    private static void resolveSeparatedFile(CloudWrappedObject cwo) {

    }

    private static void resolveFile(CloudWrappedObject cwo) {
        System.out.println("ЦВО Парсер");
        String targetPath = cwo.getTargetPath();
        Path path = Paths.get(targetPath + File.separator + cwo.getFileName());
        System.out.println(path.toString());
        if(!Files.exists(path)){
            try {
                Files.createFile(path);
                Files.write(path,cwo.getBytes());
            } catch (IOException e) {
                System.out.println("Не удалось записать файл!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл уже существует");
        }
    }




}
