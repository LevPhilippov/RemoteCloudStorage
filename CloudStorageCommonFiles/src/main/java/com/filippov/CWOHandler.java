package com.filippov;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CWOHandler {

    public static void parse(CloudWrappedObject cwo) {
        switch (cwo.getTypeEnum()) {
            case FILE: writeIntoFile(cwo); break;
            default: showError(); break;
        }
    }

    private static void showError() {
        System.out.println("Неизвестная команда или данные повреждены");
    }


    private static void writeIntoFile(CloudWrappedObject cwo) {
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
