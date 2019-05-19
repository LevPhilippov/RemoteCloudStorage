package com.filippov;

import com.filippov.HibernateUtils.FilesEntity;
import com.filippov.HibernateUtils.Utils;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServerRequestHandler{

    private static final Logger LOGGER = LogManager.getLogger(ServerRequestHandler.class.getCanonicalName());

    public static void parse(Request request, ChannelHandlerContext ctx) {
        switch (request.getRequestType()){
            case FILELIST: sendFileList(request,ctx);
                break;
            case GETFILES: {
                LOGGER.debug("Получен запрос на отправку файлов: {} ", request.getFileList());
                for (File file : request.getFileList()) {
                    ServerWrappedFileHandler.parseToSend(request.getLogin(), file, ctx.channel(), null);
                }
                break;
            }
            case DELETEFILES: {
                LOGGER.debug("Получен запрос на удаление файлов: {}", request.getFileList());
                deleteFiles(request.getLogin(), request.getFileList());
                break;
            }
            case CREATE_FOLDER: {
                LOGGER.debug("Получен запрос на создание папки: {}", request.getServerPath().getPath());
                String folderName = request.getServerPath().getName();
                String serverPath = request.getServerPath().getParent();
                String pathNameHash = Factory.MD5PathNameHash(serverPath, folderName);
                Utils.createFileRecord(request.getLogin(),
                        request.getServerPath().getParent(),
                        request.getServerPath().getName(),
                        pathNameHash,
                        request.getServerPath().getPath());
                break;
            }
            case PROPERTY: {
                LOGGER.debug("Получен запрос на свойства файла: {}", request.getServerPath().getPath());
                sendProperty(request, ctx);
                break;
            }
            default:
                LOGGER.error("Неизвестный тип Request: {}", request.getRequestType());
                break;
        }
    }

    private static void deleteFiles(String login, List<File> fileList) {
        LOGGER.trace("Зашли в метод удаления файлов: {}", fileList);
        //проверить наличие записи в базе и наличие файла на диске, затем удалить файл, затем удалить запись в базе.
        for (File file : fileList) {
            //если это папка то работаем как с папкой
            if(Utils.isThatDirectory(login, file)) {
                LOGGER.trace("Удаляем папку c содержимым!");
                //тут нужен рекурсивный метод
                List <File> fileList1 = Utils.fileList(login, file);
                LOGGER.trace("Удаляем следующие файлы : {} ", fileList1);
                deleteFiles(login, fileList1);
                Utils.deleteFileRecord(login, file);
                continue;
            }
            //если это файл - конструируем абсолютный путь к файлу и работаем с файлом
            //получаем путь из БД
            Path dbPath = Utils.getRecordedPath(login,file);
            Path serverPath = Paths.get(Server.rootPath.toString(),dbPath.toString());
            //если файл существует в БД и на сервере
            if(Files.exists(serverPath)) {
                try {
                    //удаляем запись на диске сервера
                    Files.delete(serverPath);
                    //удаляем запись в БД
                    Utils.deleteFileRecord(login, file);
                } catch (IOException e) {
                    LOGGER.error("Ошибка удаления файлов!\n" + e.getMessage() );
                }
            }
        }
    }

    /**
     * Метод выдергивает из {@link Request} адрес каталога и отправляет список файлов в этом каталоге.
     * При указании файла вместо каталога ничего не отправляется.
     * */
    private static void sendFileList(Request request, ChannelHandlerContext ctx) {
        //проверка - директория или файл (у директории есть children)
        if(Utils.isThatDirectory(request.getLogin(), request.getServerPath())){
            LOGGER.debug("Запрос на список файлов в каталоге: {}", request.getServerPath());
            List<File> fileList = Utils.fileList(request.getLogin(), request.getServerPath());
            LOGGER.debug("Отправляю список файлов в каталоге {} : {} ", request.getServerPath().toString(), fileList.toString());
            ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER)
                    .setAnswerType(Request.RequestType.FILELIST)
                    .setFileList(fileList)
            );
            return;
        }
            ServiseMessage.sendMessage(ctx.channel(), "Запрошенная директория является файлом!");
            LOGGER.debug("Запрошенная директория является файлом!");
    }

    private static void sendProperty(Request request, ChannelHandlerContext ctx) {
        Path path = Utils.getRecordedPath(request.getLogin(), request.getServerPath());
        FilesEntity filesEntity = Utils.getFileRecord(request.getLogin(), request.getServerPath());
        FileProperties fileProperty = new FileProperties(filesEntity.getFileName()
                , filesEntity.getPath()
                , Paths.get(Server.rootPath.toString(), path.toString()));
        ctx.writeAndFlush(request.setRequestType(Request.RequestType.ANSWER)
                .setAnswerType(Request.RequestType.PROPERTY)
                .setFileProperty(fileProperty));
    }

}
