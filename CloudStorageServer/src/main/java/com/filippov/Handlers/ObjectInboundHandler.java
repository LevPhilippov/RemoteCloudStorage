package com.filippov.Handlers;

import com.filippov.Request;
import com.filippov.ServerRequestHandler;
import com.filippov.WrappedFile;
import com.filippov.ServerWrappedFileHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.ReentrantLock;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(ObjectInboundHandler.class.getCanonicalName());
    private ReentrantLock locker;

    public ObjectInboundHandler(){
        locker = new ReentrantLock();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Request) {
                LOGGER.debug("Получен запрос! Отправляю в обработчик запросов!");
                ServerRequestHandler.parse((Request)msg, ctx);
            }
            else {
                WrappedFile wrappedFile = (WrappedFile) msg;
                LOGGER.debug("Получен запакованный файл! Отправляю на обработку и сохранение!");
                ServerWrappedFileHandler.parseToSave(wrappedFile, locker);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
