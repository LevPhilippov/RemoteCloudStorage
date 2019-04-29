package com.filippov;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Request) {
                Request request = (Request) msg;
                if(request.getRequestType().equals(Request.RequestType.ANSWER)) {
                    System.out.println("Получен ответ!");
                    ctx.fireChannelRead(request);
                    return;
                }

                System.out.println("Получен запрос! Отправляю в парсер запросов!");
                RequestHandler.parse((Request)msg, ctx);
            }
            else {
                WrappedFile wrappedFile = (WrappedFile) msg;
                System.out.println(wrappedFile.getMsg());
                System.out.println("Отправляю в парсер объектов!");
                WrappedFileHandler.parseToSave(wrappedFile);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
