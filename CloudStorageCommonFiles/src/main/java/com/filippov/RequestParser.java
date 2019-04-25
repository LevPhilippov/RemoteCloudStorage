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
        String path;
        if(request.getPath() == null || request.getPath().equals("root")) {
            path = "CloudStorageServer/Storage";
            System.out.println("Получен запрос на отправку списка файлов в корневом каталоге.");
        } else {path = request.getPath();}
        List<String> fileList = Factory.giveFileList(path);
        ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.FILELIST).setFileList(fileList));
        System.out.println("Отправлен список файлов");
    }

    private Request formRequest(Request.RequestType requestType, Request.RequestType answerType, String path, List<String> fileList){
        return new Request().setRequestType(requestType).setAnswerType(answerType).setPath(path).setFileList(fileList);
    }
}
