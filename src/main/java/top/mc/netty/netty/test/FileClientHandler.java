package top.mc.netty.netty.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.mc.netty.bean.ResultBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/27 15:51
 * @description：
 */
public class FileClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg; // (1)
        try {
            System.out.println(in.readableBytes());
            byte[] array = new byte[in.readableBytes()];
            in.readBytes(array);
            ByteArrayInputStream byteInt=new ByteArrayInputStream(array);
            ObjectInputStream objInt=new ObjectInputStream(byteInt);
            ResultBean resultBean=null;
            resultBean=(ResultBean)objInt.readObject();
            //由于选择ChannelOutboundHandlerAdapter的read方法，而每次出站都会调用这个channel的read所以这里删除这个channel.
            ctx.pipeline().remove("fileByteClient");
            System.out.println(resultBean.getMsg());
            ctx.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            in.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
