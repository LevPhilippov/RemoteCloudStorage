package com.filippov;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Factory {

    public static List<String> giveFileList(String path) {
        List<String> fileList = new ArrayList<>();
        try {
            Files.walk(Paths.get(path),1).forEach(path1 -> {
                fileList.add(path1.getFileName().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

}
