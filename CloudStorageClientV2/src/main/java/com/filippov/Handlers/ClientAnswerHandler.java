package com.filippov.Handlers;

import com.filippov.ClientMain;
import com.filippov.Network;
import com.filippov.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.util.function.Consumer;

public class ClientAnswerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            Request request = (Request) msg;
            switch (request.getAnswerType()) {
                case FILELIST: {
                    System.out.println("Обновляю список файлов на сервере " + request.getFileList().toString());
                    Network.getInstance().getPathHolder().setServerPath(request.getServerPath());
                    Network.getInstance().getController().refreshServerFileList(request.getFileList());
                    break;
                }
                case AUTH_SUCCESS: {
                    System.out.println("Авторизация успешна!");
                    ClientMain.clientMain.setMainScene();
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
