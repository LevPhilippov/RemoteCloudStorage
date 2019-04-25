import com.filippov.CloudObjectWrapper;
import com.filippov.CloudWrappedObject;
import com.filippov.ObjectInboundHandler;
import com.filippov.Request;
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
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Network{

    private static Network ourInstance = new Network();
    public static Network getInstance() {return ourInstance;}
    private Network(){}

    private ChannelFuture cf;
    private EventLoopGroup bossGroup = new NioEventLoopGroup(2);
    private Bootstrap bootstrap = new Bootstrap();
    private static Controller controller;


    public void startNetwork() {
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler("EndLogger", LogLevel.INFO),
                                    new ObjectDecoder(1024*1024*10,ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new ObjectInboundHandler(),
//                                    new ClientOutboundHandler(),
                                    new ClientAnswerHandler()
                            );
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);

                    cf = bootstrap.connect("127.0.0.1", 8189);
                    cf.addListener((ChannelFutureListener) channelFuture -> {
                        if(!channelFuture.isSuccess()) {
                            System.out.println("Connnection failed!");
                        } else {
                            System.out.println("Connection success!");
                            requestFilesList("root");
                        }

                    }).sync();
                    cf.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    shutdown();
                }
            }
        });
        networkThread.setDaemon(true);
        networkThread.start();
    }

    public ChannelFuture getCf() {
        return cf;
    }

    public void shutdown() {
        cf.channel().closeFuture();
        bossGroup.shutdownGracefully();
    }

    public void synchronize(ObservableList<String> os) {
        Path homePath = Paths.get("CloudStorageClientV2/Storage");
//        Path destPath = Paths.get("CloudStorageServer/Storage");
        for (String o : os) {
            Path path = Paths.get(homePath.toString() + File.separator + o);
            System.out.println(path.toString());
            System.out.println(Files.exists(path));
            try {
                CloudWrappedObject cwo =  CloudObjectWrapper.wrapFile(path, o);
                cf.channel().writeAndFlush(cwo);
            } catch (IOException a) {
                System.out.println("Ошибка записи");
                a.printStackTrace();
            } finally {
                requestFilesList("root");
            }
        }
    }

    public Controller getController() {
        return controller;
    }

    public static void setController(Controller controller) {
        Network.controller = controller;
    }

    public void requestFilesList(String path) {
        cf.channel().writeAndFlush(new Request().setRequestType(Request.RequestType.FILELIST).setPath(path));
    }

    public void requestFile(ObservableList<String> os) {

    }

}