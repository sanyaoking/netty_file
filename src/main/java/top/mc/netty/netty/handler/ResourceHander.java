package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.address.DynamicAddressConnectHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import top.mc.netty.bean.ResultBean;

import java.io.*;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Future;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 15:04
 * @description：最后一个handler释放资源
 */
public class ResourceHander extends SimpleChannelInboundHandler<HttpContent> {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("ResourceHander    exceptionCaught");
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }
    private void writeResponse(Channel channel, boolean forceClose) {
        AttributeKey<ResultBean> reusltBean = AttributeKey.valueOf("resultBean");
        ResultBean resultBean = channel.attr(reusltBean).get();
        // Convert the response content to a ChannelBuffer.
        ByteArrayOutputStream byt=new ByteArrayOutputStream();

        ObjectOutputStream obj= null;
        try {
            obj = new ObjectOutputStream(byt);
            obj.writeObject(resultBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ByteBuf buf = copiedBuffer(byt.toByteArray());
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ChannelFuture future = channel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws Exception {
        AttributeKey<ResultBean> reusltBean = AttributeKey.valueOf("resultBean");
        ResultBean resultBean = ctx.channel().attr(reusltBean).get();
        if(resultBean==null){
            resultBean = new ResultBean();
            resultBean.setMsg("this is test!");
        }
        ChannelFuture cf = ctx.channel().writeAndFlush(resultBean);
        cf.addListener(ChannelFutureListener.CLOSE);
    }
}
