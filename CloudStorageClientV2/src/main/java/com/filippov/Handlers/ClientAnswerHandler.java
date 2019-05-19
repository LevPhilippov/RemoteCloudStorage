package com.filippov.Handlers;

import com.filippov.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientAnswerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(ClientWrappedFileHandler.class.getCanonicalName());
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Request request = (Request) msg;
            switch (request.getAnswerType()) {
                case FILELIST: {
                    LOGGER.debug("Обновляю список файлов на сервере {}", request.getFileList().toString());
                    Network.messageService.setSingleServiseMessage("Обновляю список файлов на сервере!");
                    Controller.controller.refreshServerFileList(request.getFileList(), request.getServerPath().toPath());
                    break;
                }
                case AUTH_SUCCESS: {
                    Network.messageService.setSingleServiseMessage("Авторизация успешна!");
                    LOGGER.info("Авторизация успешна!");
                    ClientMain.clientMain.setMainScene();
                    break;
                }
                case PROPERTY: {
                    Runnable runnable = () -> {
                        CreateControllerGUI.showFileProperty(request.getFileProperty());
                    };
                    Controller.refreshPattern(runnable);
                    break;
                }
                default: {
                    LOGGER.info("Неизвестный ответ! Обновите версию клиентского приложения!");
                    break;
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
