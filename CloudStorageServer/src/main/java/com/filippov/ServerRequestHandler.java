package com.filippov;

import com.filippov.HibernateUtils.Utils;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.util.List;

public class ServerRequestHandler{

    public static void parse(Request request, ChannelHandlerContext ctx) {

        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES:
                System.out.println("Получен запрос на отправку файлов: " + request.getFileList());
//                filesWork(request, ctx);
                break;
            case DELETEFILES:
                System.out.println("Удаление файлов! " + request.getFileList().toString());
//                filesWork(request, ctx);
                break;
            case CREATE_FOLDER:
                Utils.createFolder();
            default:
                System.out.println("Неизвестный тип Request");
                break;
        }
    }

    /**
     * Метод выдергивает из {@link Request} адрес каталога и отправляет список файлов в этом каталоге.
     * При указании файла вместо каталога ничего не отправляется.
     * */
    private static void sendFileList(Request request, ChannelHandlerContext ctx) {
        System.out.println("Запрос на список файлов в каталоге: " + request.getServerPath().getParent());
        List<File> fileList = Utils.fileList(request.getLogin(), request.getServerPath().getParent());
            System.out.println("Отправляю список файлов в каталоге " + request.getServerPath().toString() + ": " + fileList.toString());
            ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER)
                            .setAnswerType(Request.RequestType.FILELIST)
                            .setFileList(fileList)
                            );
    }

}
