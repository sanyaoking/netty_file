package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import top.mc.netty.bean.ResultBean;
import top.mc.netty.netty.busAtomHandler.FileByteServerHandler;
import top.mc.netty.netty.busAtomHandler.HttpDownFileServerHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ：mengchao
 * @date ：Created in 2019/10/16 21:06
 * @description：为了保存原始数据
 */
public class HttpRequestDecoderByteMC extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buffer = (ByteBuf) msg;
            ByteBuf buffer1 = buffer.copy();
            while (buffer1.isReadable()) {
                byte[] q = new byte[buffer1.readableBytes()];
                buffer1.readBytes(q);
                byte[] flag = new byte[3];
                for(int i=0;i<q.length&&i<3;i++){
                    flag[i]=q[i];
                }
                String f = new String(flag);
                if("GET".equalsIgnoreCase(f)){//http请求
                    ctx.pipeline().addLast("1",new HttpRequestDecoder());
                    //HttpObjectAggregator 将多个消息转换为单一的一个FullHttpRequest，控制请求的大小
                    ctx.pipeline().addLast("2",new HttpObjectAggregator(65536*5));
                    //用于压缩数据(官方网站有这个东西，具体如何使用未知)
                    ctx.pipeline().addLast("3",new HttpContentCompressor());
                    if (ctx.pipeline().get("httpResponseEncoder") == null) {
                        ctx.pipeline().addLast("httpResponseEncoder", new HttpResponseEncoder());
                    }
                    if (ctx.pipeline().get("chunkedWriteHandler") == null) {
                        ctx.pipeline().addLast( "chunkedWriteHandler", new ChunkedWriteHandler());
                    }
                    if (ctx.pipeline().get("httpDownFileServerHandler") == null) {
                        ctx.pipeline().addLast( "httpDownFileServerHandler", new HttpDownFileServerHandler());
                    }
                    System.out.println("添加下载文件httpDownFileServerHandler");
                    ctx.pipeline().remove("httpRequestDecoderByteMC");
                    ctx.fireChannelRead(msg);
                }else{//上传文件的请求
                    ctx.pipeline().addLast("fileByteServerHandler",new FileByteServerHandler());
                    ctx.pipeline().remove("httpRequestDecoderByteMC");
                    ctx.fireChannelRead(msg);
                }
            }
            buffer1.release();
        }
//        super.channelRead(ctx, msg);
    }
}
