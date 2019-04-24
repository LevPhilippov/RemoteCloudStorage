package handlers;

import com.filippov.CWOParser;
import com.filippov.CloudWrappedObject;
import com.filippov.TypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;


public class ObjectInboundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CloudWrappedObject cwo = (CloudWrappedObject) msg;
            System.out.println(cwo.getMsg());
            System.out.println("READ");
            if(cwo.getTypeEnum().equals(TypeEnum.ECHO)) {
                System.out.println("Ответ");
                ctx.writeAndFlush(msg);
            } else {
                System.out.println("Отправляю в парсер!");
                CWOParser.parselResolver(cwo);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

}
