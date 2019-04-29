package com.filippov;

import io.netty.channel.Channel;
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

public class RequestHandler {

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES:
                System.out.println("Получен запрос на отправку файлов: " + request.getFileList());
                tryTo(request, ctx);
                break;
            case DELETEFILES:
                System.out.println("Удаление файлов! " + request.getFileList().toString());
                tryTo(request, ctx);
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


//    public static void writeFilesIntoChannel(Request request, ChannelHandlerContext ctx) {
//        List<File> filesList = request.getFileList();
//        System.out.println("Получен запрос на отправку файлов: " + filesList);
//        filesList.stream().map((file) -> file.toPath()).forEach(path -> {
//            if(Files.exists(path)) {
//                System.out.println("Файл " + path.getFileName().toString() + " найден!");
//                try {
//                    Files.walkFileTree(path, new MyFileVisitor(request.getServerPath(), request.getClientPath(), ctx.channel(), Request.RequestType.GETFILES));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

//    public static void deleteFiles(Request request) {
//        System.out.println("Удаление файлов! " + request.getFileList().toString());
//        request.getFileList().stream().map((file -> file.toPath())).forEach();
//    }

    public static void tryTo (Request request, ChannelHandlerContext ctx) {
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
