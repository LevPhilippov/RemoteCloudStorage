package com.filippov;

import com.filippov.HibernateUtils.Utils;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            case DELETEFILES: {
                System.out.println("Удаление файлов! " + request.getFileList());
                //проверить наличие записи в базе и наличие файла на диске, затем удалить файл, затем удалить запись в базе.
                for (File file : request.getFileList()) {
                    //получаем путь из БД
                    Path dbPath = Utils.getRecordPath(request.getLogin(),file);
                    //если это папка то работаем как с папкой
                    if(Utils.isThatDirectory(request.getLogin(), file)) {
                        System.out.println("Удаляем папку!");
                        continue;
                    }
                    //если это файл - конструируем абсолютный путь к файлу и работаем с файлом
                    Path serverPath = Paths.get(Server.rootPath.toString(),dbPath.toString());
                    //если файл существует в БД и на сервере
                    if(Files.exists(serverPath)) {
                        try {
                            //удаляем запись на диске сервера
                            Files.delete(serverPath);
                            //удаляем запись в БД
                            Utils.deleteFileRecord(request.getLogin(), file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            }
            case CREATE_FOLDER: {
                String folderName = request.getServerPath().getName();
                String serverPath = request.getServerPath().getParent();
                String pathNameHash = DigestUtils.md5Hex(serverPath + folderName);
                Utils.createFileRecord(request.getLogin(),
                        request.getServerPath().getParent(),
                        request.getServerPath().getName(),
                        pathNameHash,
                        request.getServerPath().getPath());
            }
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
        //проверка - директория или файл (у директории есть children)
        if(Utils.isThatDirectory(request.getLogin(), request.getServerPath())){
            System.out.println("Запрос на список файлов в каталоге: " + request.getServerPath());
            List<File> fileList = Utils.fileList(request.getLogin(), request.getServerPath());
            System.out.println("Отправляю список файлов в каталоге " + request.getServerPath().toString() + ": " + fileList.toString());
            ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER)
                    .setAnswerType(Request.RequestType.FILELIST)
                    .setFileList(fileList)
            );
            return;
        }
            System.out.println("Запрошенная директория является файлом! Здесь могла быть ваша реклама!");
    }

}