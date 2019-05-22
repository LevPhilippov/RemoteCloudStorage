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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private SslContext sslCtx;
    private static final Logger LOGGER = LogManager.getLogger(ServerChannelInitializer.class.getCanonicalName());

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
            setupSSL();
            socketChannel.pipeline().addLast(
                    sslCtx.newHandler(socketChannel.alloc()),
                    new LoggingHandler("HeadLogger", LogLevel.TRACE),
                    new ObjectEncoder(),
                    new ObjectDecoder(ServerWrappedFileHandler.byteBufferSize + 1024*1024, ClassResolvers.softCachingConcurrentResolver(null)),
                    new LoggingHandler("AuthLogger", LogLevel.DEBUG),
                    new AuthHandler(),
                    new LoggingHandler("ObjectLogger", LogLevel.DEBUG),
                    new ObjectInboundHandler());
    }

    private void setupSSL() {
        try {
            // Configure SSL
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            //SSL
        } catch (SSLException e) {
            LOGGER.error("Не получилось создать sslContext!" + e.getMessage());
        } catch (CertificateException e) {
            LOGGER.error("Ошибка SelfSignedCertificate!" + e.getMessage());
        }
    }

}
