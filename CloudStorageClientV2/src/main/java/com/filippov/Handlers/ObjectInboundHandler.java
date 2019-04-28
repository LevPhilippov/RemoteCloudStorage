package com.filippov.Handlers;

import com.filippov.*;
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
                CloudWrappedObject cwo = (CloudWrappedObject) msg;
                System.out.println(cwo.getMsg());
                System.out.println("Отправляю в парсер объектов!");
                CWOHandler.parse(cwo);
            }
        } finally {
            Network.getInstance().getController().refreshLocalFileList();
            ReferenceCountUtil.release(msg);
        }
    }
}
