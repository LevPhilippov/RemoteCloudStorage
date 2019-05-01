package com.filippov.Handlers;

import com.filippov.AuthData;
import com.filippov.HibernateUtils.Utils;
import com.filippov.Request;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean autorizedClient;
    private int autorizationCounter;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(autorizedClient)
            ctx.fireChannelRead(msg);
        else {
            if(msg instanceof AuthData) {
                autorizedClient = true;
                System.out.println("Клиент авторизован");
                Utils.write((AuthData)msg);
                ctx.writeAndFlush(new Request().setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.AUTH_SUCCESS));
            }
            else {
                System.out.println("Клиент не авторизован!");
                autorizationCounter++;
            }
            ReferenceCountUtil.release(msg);
        }

        if (autorizationCounter>=3) {
            ctx.channel().closeFuture().sync();
        }
    }
}
