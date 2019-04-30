package com.filippov;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class RequestHandler {

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES:
                System.out.println("Получен запрос на отправку файлов: " + request.getFileList());
                filesWork(request, ctx);
                break;
            case DELETEFILES:
                System.out.println("Удаление файлов! " + request.getFileList().toString());
                filesWork(request, ctx);
                break;
            case AUTH:
                System.out.println("Запрос авторизации!");
                break;
            default:
                System.out.println("Неизвестный тип Request");
                break;
        }
    }

    /**
     * Метод выдергивает из {@link Request} адрес каталога и отправляет список файлов в этом каталоге.
     * При указании файла вместо каталога ничего не отправляется.
     * */
    public static void sendFileList(Request request, ChannelHandlerContext ctx) {
        String rootPath = "CloudStorageServer/Storage";
        File path = (request.getServerPath()==null)? new File(rootPath): request.getServerPath();
        if (path.exists()){
            List<File> fileList = Factory.giveFileList(path);
            System.out.println("Отправляю список файлов в каталоге " + path.toString() + ": " + fileList.toString());
            ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER)
                            .setAnswerType(Request.RequestType.FILELIST)
                            .setFileList(fileList)
                            .setServerPath(path)
                            .setClientPath(null)
                            );
            return;
        }

        System.out.println("Запрошенный путь  " + request.getServerPath() + " не является каталогом");
    }

    /**
     * Записывает в канал объект авторизации с хэшированными логином и паролем
     * Хэширование выполяется автоматически в объекте AuthData
     * */
    public static void hashAndSendAuthData(String login, String password, Channel channel) {
        System.out.println("Отправлены авторизационные данные!");
        channel.writeAndFlush(new AuthData(login, password));
    }



    public static void filesWork(Request request, ChannelHandlerContext ctx) {
        request.getFileList().stream().map((file) -> file.toPath()).forEach(path -> {
            if(Files.exists(path)) {
                System.out.println("Файл " + path.getFileName().toString() + " найден!");
                try {
                    Files.walkFileTree(path, new MyFileVisitor(request.getServerPath(), request.getClientPath(), ctx.channel(), request.getRequestType()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
