package top.mc.netty.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/25 20:23
 * @description：responseEncoder编码器
 */
public class HttpResponseEncoderMC extends HttpResponseEncoder {
    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }
}
