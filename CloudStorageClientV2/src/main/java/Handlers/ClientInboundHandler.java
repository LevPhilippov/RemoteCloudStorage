package Handlers;

import com.filippov.CWOParser;
import com.filippov.CloudWrappedObject;
import com.filippov.TypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CloudWrappedObject cwo = (CloudWrappedObject) msg;
            if(cwo.getTypeEnum().equals(TypeEnum.ECHO)) {
                System.out.println("ECHO");
            } else {
                CWOParser.parselResolver(cwo);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
