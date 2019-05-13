package com.filippov.Handlers;

import com.filippov.AuthData;
import com.filippov.HibernateUtils.Utils;
import com.filippov.Request;
import com.filippov.WrappedFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.File;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean autorizedClient;
//    private int autorizationCounter;
    private String login;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //если клиент авторизован
        if(autorizedClient){
            //если msg это запакованный файл или реквест - дописываем в файл домашнюю директорию для этого клиента (это логин в MD5)
            if(msg instanceof WrappedFile) {
                ((WrappedFile)msg).setLogin(login);
            }
            if(msg instanceof Request)
                ((Request)msg).setLogin(login);

            ctx.fireChannelRead(msg);
        }
        //если это авторизационные данные AuthData
        else {
            if(msg instanceof AuthData && Utils.checkAuthData((AuthData)msg)) {
                autorizedClient = true;
                System.out.println("Клиент авторизован");
                login = ((AuthData)msg).getLogin();
                ctx.writeAndFlush(new Request().setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.AUTH_SUCCESS));
            }
            else {
                System.out.println("Клиент не авторизован!");
//                autorizationCounter++;
            }
            ReferenceCountUtil.release(msg);
        }
//
//        if (autorizationCounter>3) {
//            ctx.channel().closeFuture().sync(); //как закрыть соединение отсюда???
//        }
    }
}
