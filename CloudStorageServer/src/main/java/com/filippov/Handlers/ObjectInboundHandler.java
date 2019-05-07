package com.filippov.Handlers;

import com.filippov.Request;
import com.filippov.ServerRequestHandler;
import com.filippov.WrappedFile;
import com.filippov.ServerWrappedFileHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Request) {
                System.out.println("Получен запрос! Отправляю в обработчик запросов!");
                ServerRequestHandler.parse((Request)msg, ctx);
            }
            else {
                WrappedFile wrappedFile = (WrappedFile) msg;
                System.out.println("Получен запакованный файл! Отправляю на обработку и сохранение!");
                ServerWrappedFileHandler.parseToSave(wrappedFile);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
