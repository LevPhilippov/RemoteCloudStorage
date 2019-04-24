import Handlers.ClientInboundHandler;
import Handlers.ClientOutboundHandler;
import Handlers.TestHandler;
import com.filippov.CloudWrappedObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Network implements Runnable{
    private ChannelFuture cf;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private Bootstrap bootstrap = new Bootstrap();

    public void run(){
        try {
            bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(
                        new LoggingHandler("EndLogger", LogLevel.INFO),
                        new ObjectDecoder(1024*1024*10,ClassResolvers.cacheDisabled(null)),
                        new ObjectEncoder(),
                        new ClientOutboundHandler(),
                        new ClientInboundHandler()
                        );
            }
        }).option(ChannelOption.SO_KEEPALIVE, true);

        cf = bootstrap.connect("127.0.0.1", 8189);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if(!channelFuture.isSuccess()) {
                System.out.println("Connnection failed!");
            } else {
                System.out.println("Connection success!");
                channelFuture.channel().remoteAddress();
            }

            }
        }).sync();
        cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();

    }

    public ChannelFuture getCf() {
        return cf;
    }
}
