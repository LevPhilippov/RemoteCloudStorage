package com.filippov;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Factory {

    public static List<String> giveFileList(String path) {
        List<String> fileList = new ArrayList<>();
        try {
            Files.walk(Paths.get(path),1).forEach(p -> {
                fileList.add(p.getFileName().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
