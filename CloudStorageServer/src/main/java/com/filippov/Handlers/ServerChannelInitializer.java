package com.filippov.Handlers;

import com.filippov.ServerWrappedFileHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private SslContext sslCtx;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
            setupSSL();
            socketChannel.pipeline().addLast(
                    sslCtx.newHandler(socketChannel.alloc()),
                    new LoggingHandler("commonLog", LogLevel.INFO),
                    new ObjectEncoder(),
                    new ObjectDecoder(ServerWrappedFileHandler.byteBufferSize + 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                    new AuthHandler(),
                    new ObjectInboundHandler());
    }

    private void setupSSL() {
        try {
            // Configure SSL
            SelfSignedCertificate ssc = new SelfSignedCertificate();  //-What is it?
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            //SSL
        } catch (SSLException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

    }

}
