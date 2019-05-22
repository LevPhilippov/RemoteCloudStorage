package com.filippov;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

    public static String MD5FileHash(Path path) {
        try {
            return DigestUtils.md5Hex(new FileInputStream(path.toFile()));
        } catch (IOException e) {
            System.out.println("File not found!");
        }
        return null;
    }


}
