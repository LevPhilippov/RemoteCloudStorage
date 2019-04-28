package com.filippov;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RequestHandler {

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES: writeFilesIntoChannel(request, ctx);
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
        String path= "CloudStorageServer/Storage";
        if(request.getServerPath() == null || request.getServerPath().equals("root")) {
            System.out.println("Получен запрос на отправку списка файлов в корневом каталоге.");
        } else {path = request.getServerPath();}
        if (Files.isDirectory(Paths.get(path))){
            List<String> fileList = Factory.giveFileList(path);
            ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.FILELIST).setFileList(fileList).setServerPath(path));
            System.out.println("Отправлен список файлов в каталоге " + path);
        }
        System.out.println("Запрошенный путь не является каталогом");
    }


    public static void writeFilesIntoChannel(Request request, ChannelHandlerContext ctx) {
//        String localPath = request.getServerPath();
//        String targetPath = request.getClientPath();
        List<String> filesList = request.getFileList();
        System.out.println("Получен запрос на отправку файлов: " + filesList);
        filesList.stream().map((s) -> Paths.get(request.getServerPath() + "/" + s)).forEach(path -> {
            if(Files.exists(path)) {
                System.out.println("Файл " + path.getFileName().toString() + "существует");
                CloudWrappedObject cwo = null;
                try {
                    Files.walkFileTree(path, new MyFileVisitor(request.getServerPath(), request.getClientPath(), ctx.channel()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
