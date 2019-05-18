package com.filippov.Handlers;

import com.filippov.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof ServiseMessage) {
                System.out.println("Получено сервисное сообщение!");
                Network.messageService.setSingleServiseMessage(((ServiseMessage)msg).getMessage());
            }
            else if (msg instanceof Request) {
                Request request = (Request) msg;
                if(request.getRequestType().equals(Request.RequestType.ANSWER)) {
                    System.out.println("Получен ответ!");
                    ctx.fireChannelRead(request);
                    return;
                }
            } else {
                WrappedFile wrappedFile = (WrappedFile) msg;
                System.out.println("Отправляю в парсер объектов!");
                ClientWrappedFileHandler.parseToSave(wrappedFile);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
