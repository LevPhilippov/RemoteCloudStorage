package com.filippov;

import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Factory {

    public static List<Path> giveFileList(Path path) {
        List<Path> fileList = new ArrayList<>();
        System.out.println(path.toString());
        try {
            Files.walk(path,1).forEach(p -> {
                fileList.add(p);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileList.remove(0);
        return fileList;
    }


//    public static String giveStepBackPath(String oldPath) {
//        System.out.println("Меняю старый путь: " + oldPath);
//        StringBuilder builder = new StringBuilder();
//
//        char[] chars = oldPath.toCharArray();
//        for (int i = 0; i <chars.length ; i++) {
//            chars[i] = (chars[i]=='\\')? '/': chars[i];
//        }
//        oldPath = new String(chars);
//
//        String[] oldPathArray = oldPath.split("/");
//        String[] newPathArray = Arrays.copyOfRange(oldPathArray,0,oldPathArray.length-1);
//        Arrays.stream(newPathArray).forEach((s)-> {
//            builder.append(s + '/');
//        });
//        String newPath = builder.toString();
//        System.out.println("Новый путь: " + newPath);
//        return newPath;
//    }



}
