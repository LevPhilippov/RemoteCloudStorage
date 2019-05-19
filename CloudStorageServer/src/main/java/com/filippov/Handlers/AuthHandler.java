package com.filippov.Handlers;

import com.filippov.*;
import com.filippov.HibernateUtils.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean autorizedClient;
    private String login;
    private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class.getCanonicalName());

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
            if(msg instanceof AuthData) {
                AuthData authData = (AuthData)msg;
                if(!authData.isNewId()) {
                    //существующий юзер
                    if(!Utils.checkAuthData((AuthData)msg)){
                        //если логин и пароль не верны
                        LOGGER.info("Клиент не авторизован! {}", ((AuthData) msg).getLogin());
                        ServiseMessage.sendMessage(ctx.channel(), "Авторизация невозможна! Неверный логин или пароль!");
                        ReferenceCountUtil.release(msg);
                        return;
                    }
                } else {
                    LOGGER.info("Попытка создания нового пользователя! {}", ((AuthData) msg).getLogin());
                    //новый юзер
                    if(!Utils.writeNewClientAuthData(authData)) {
                        //если пользователь с такими данными уже зарегистрирован
                        LOGGER.info("Пользователь с именем {} уже существует!", authData.getLogin());
                        ServiseMessage.sendMessage(ctx.channel(), "Пользователь с таким ником уже существует!");
                        ReferenceCountUtil.release(msg);

                        return;
                    }
                }
                //создание директории пользователя (при регистрации нового пользователя)
                if (!Files.exists(Paths.get(Server.rootPath.toString(), authData.getLogin()))) {
                    Files.createDirectory(Paths.get(Server.rootPath.toString(), authData.getLogin()));
                }
                //если логин и пароль верны или успешно зарегистрировался новый пользователь
                autorizedClient = true;
                LOGGER.info("Клиент {} авторизован!", authData.getLogin());
                login = ((AuthData)msg).getLogin();
                ctx.writeAndFlush(new Request().setRequestType(Request.RequestType.ANSWER).setAnswerType(Request.RequestType.AUTH_SUCCESS));
            }
        }
    }
}
