package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import top.mc.netty.bean.ResultBean;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/23 11:14
 * @description：文件上传客户端解码器
 */
public class UploadDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        byte[] array = new byte[in.readableBytes()];
        in.readBytes(array);
        ByteArrayInputStream byteInt=new ByteArrayInputStream(array);
        ObjectInputStream objInt=new ObjectInputStream(byteInt);
        ResultBean resultBean=null;
        resultBean=(ResultBean)objInt.readObject();
        out.add(resultBean);
    }
}
