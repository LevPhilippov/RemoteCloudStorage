package com.filippov.Handlers;

import com.filippov.AuthData;
import com.filippov.HibernateUtils.Utils;
import com.filippov.Request;
import com.filippov.Server;
import com.filippov.WrappedFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean autorizedClient;
    private int autorizationCounter;
    private String userHomeDir;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(autorizedClient){
            if(msg instanceof WrappedFile) {
                ((WrappedFile)msg).setHomeDir(Paths.get(Server.rootPath.toString(),userHomeDir).toFile());
                System.out.println(((WrappedFile)msg).getHomeDir().toString());
            }
            ctx.fireChannelRead(msg);
        }
        else {
            if(msg instanceof AuthData && Utils.checkAuthData((AuthData)msg)) {
                autorizedClient = true;
                System.out.println("Клиент авторизован");
                userHomeDir = ((AuthData)msg).getLogin();
                ctx.writeAndFlush(new Request().setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.AUTH_SUCCESS));
            }
            else {
                System.out.println("Клиент не авторизован!");
                autorizationCounter++;
            }
            ReferenceCountUtil.release(msg);
        }

        if (autorizationCounter>3) {
            ctx.channel().closeFuture().sync(); //как закрыть соединение отсюда???
        }
    }
}
