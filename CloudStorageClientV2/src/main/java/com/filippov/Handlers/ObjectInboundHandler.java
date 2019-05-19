package com.filippov.Handlers;

import com.filippov.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(ObjectInboundHandler.class.getCanonicalName());
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof ServiseMessage) {
                LOGGER.debug("Получено сервисное сообщение!");
                Network.messageService.setSingleServiseMessage(((ServiseMessage)msg).getMessage());
            }
            else if (msg instanceof Request) {
                Request request = (Request) msg;
                if(request.getRequestType().equals(Request.RequestType.ANSWER)) {
                    LOGGER.debug("Получено получен ответ!");
                    ctx.fireChannelRead(request);
                    return;
                }
            } else {
                WrappedFile wrappedFile = (WrappedFile) msg;
                LOGGER.trace("Отправляю в парсер объектов!");
                ClientWrappedFileHandler.parseToSave(wrappedFile);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
