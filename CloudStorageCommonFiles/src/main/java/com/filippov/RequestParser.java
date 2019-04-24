package com.filippov;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RequestParser {

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            default:
                System.out.println("Неизвестный тип Request");
                break;
        }
    }





    public static void sendFileList(Request request, ChannelHandlerContext ctx) {
        String path = request.getPath();
        List<String> fileList = new ArrayList<>();
        try {
            Files.walk(Paths.get(path),1).forEach(path1 -> {
                fileList.add(path1.getFileName().toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        request.setRequestType(Request.Requests.ANSWER);
        request.setFileList(fileList);
        ctx.writeAndFlush(request);
    }
}
