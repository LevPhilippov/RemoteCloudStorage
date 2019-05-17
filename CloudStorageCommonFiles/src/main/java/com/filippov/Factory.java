package com.filippov;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.digest.DigestUtils;

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

    public static String MD5PathNameHash (String filePath, String fileName) {
        return DigestUtils.md5Hex(filePath+fileName);
    }
}
