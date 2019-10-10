package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import top.mc.netty.bean.ResultBean;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/23 11:08
 * @description：文件服务器返回信息编码
 * 注意
 * 1、无论是编码器还是解码器，其接受的消息类型必须要与待处理的参数类型一致，否则该编码器或解码器并不会执行。
 * 2、在解码器进行数据解码时，一定要记得判断缓冲（ByteBuf）中的数据是否 足够，否则将会产生一些问题。
 */
public class UploadEncoder extends MessageToByteEncoder<ResultBean> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ResultBean msg, ByteBuf out) throws Exception {
        System.out.println(msg.toString());
        System.out.println(msg.toString().getBytes("utf-8"));
        ByteArrayOutputStream baos =new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(msg);
        byte [] msg_byte = baos.toByteArray();
        out.writeBytes(msg_byte);
    }
}
