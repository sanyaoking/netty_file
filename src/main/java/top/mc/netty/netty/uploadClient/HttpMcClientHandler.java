package top.mc.netty.netty.uploadClient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;
import top.mc.netty.bean.ResultBean;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/23 10:28
 * @description：解析服务端返回信息
 */
public class HttpMcClientHandler extends SimpleChannelInboundHandler<ResultBean> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResultBean msg) throws Exception {
        if("0".equals(msg.getErrcode())){//操作成功
            System.out.println("msg:"+msg.toString());
        }else{//操作失败
            System.out.println("msg:"+msg.getMsg());
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
