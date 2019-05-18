package com.filippov.Handlers;

import com.filippov.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientAnswerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Request request = (Request) msg;
            switch (request.getAnswerType()) {
                case FILELIST: {
                    System.out.println("Обновляю список файлов на сервере " + request.getFileList().toString());
                    Network.messageService.setSingleServiseMessage("Обновляю список файлов на сервере!");
                    Controller.controller.refreshServerFileList(request.getFileList(), request.getServerPath().toPath());
                    break;
                }
                case AUTH_SUCCESS: {
                    Network.messageService.setSingleServiseMessage("Авторизация успешна!");
                    System.out.println("Авторизация успешна!");
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
                    System.out.println("Неизвестный ответ");
                    break;
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
