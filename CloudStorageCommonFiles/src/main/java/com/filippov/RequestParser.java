package com.filippov;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

public class RequestParser {

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES: writeFilesIntoCTX(request, ctx);
                break;
            default:
                System.out.println("Неизвестный тип Request");
                break;
        }
    }


    public static void sendFileList(Request request, ChannelHandlerContext ctx) {
        String path;
        if(request.getServerPath() == null || request.getServerPath().equals("root")) {
            path = "CloudStorageServer/Storage";
            System.out.println("Получен запрос на отправку списка файлов в корневом каталоге.");
        } else {path = request.getServerPath();}
        List<String> fileList = Factory.giveFileList(path);
        ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.FILELIST).setFileList(fileList));
        System.out.println("Отправлен список файлов в каталоге " + path);
    }


    public static void writeFilesIntoCTX(Request request, ChannelHandlerContext ctx) {
        System.out.println("Получен запрос на отправку файлов");
        String serverPath = request.getServerPath();
        String clientPath = request.getClientPath();
        List<String> filesList = request.getFileList();
        filesList.stream().map((s) -> Paths.get(serverPath + File.separator + s)).forEach(path -> {
            if(Files.exists(path)) {
                System.out.println("Файл " + path.toString() + "существует");
                System.out.println(path.getFileName().toString());
                CloudWrappedObject cwo = null;
                try {
                    cwo = CloudObjectWrapper.wrapFile(path,serverPath,clientPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ctx.writeAndFlush(cwo).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        System.out.println("Writing Complete!");
                    }
                });
            }
        });
    }
}
