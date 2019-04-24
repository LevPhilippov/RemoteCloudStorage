package handlers;
import com.filippov.CWOParser;
import com.filippov.CloudWrappedObject;
import com.filippov.Request;
import com.filippov.RequestParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Request) {
                System.out.println("Отправляю в парсер запросов!");
                RequestParser.parse((Request)msg, ctx);
            }
            else {
                CloudWrappedObject cwo = (CloudWrappedObject) msg;
                System.out.println(cwo.getMsg());
                System.out.println("Отправляю в парсер объектов!");
                CWOParser.parselResolver(cwo);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
