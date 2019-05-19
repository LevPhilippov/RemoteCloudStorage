package com.filippov.Handlers;

import com.filippov.ClientWrappedFileHandler;
import com.filippov.Network;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class ClientHandlersInitializer  extends ChannelInitializer <SocketChannel>  {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // Configure SSL.
        final SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        //SSL
        socketChannel.pipeline().addLast(
                sslCtx.newHandler(socketChannel.alloc(), Network.HOST, Network.PEER_PORT),
                new LoggingHandler("HeadHandler", LogLevel.TRACE),
                new ObjectDecoder(ClientWrappedFileHandler.byteBufferSize+1024*1024, ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new LoggingHandler("ObjectHandler",LogLevel.DEBUG),
                new ObjectInboundHandler(),
                new LoggingHandler("AnswerHandler",LogLevel.DEBUG),
                new ClientAnswerHandler()
        );
    }
}
