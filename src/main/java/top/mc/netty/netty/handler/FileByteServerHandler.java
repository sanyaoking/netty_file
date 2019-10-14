package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import top.mc.netty.bean.ResultBean;
import top.mc.netty.bean.UploadFile;

import java.io.*;
import java.util.ArrayList;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/27 14:10
 * @description：字节流上传文件
 */
public class FileByteServerHandler  extends ChannelInboundHandlerAdapter {
    private ArrayList<byte[]> array=new ArrayList<byte[]>();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //一起请求分多次传包，只调用一次；一次通讯多次会调用多次,发送一起请求，但是服务端返回的信息，被客户端接收后，这个方法还会被调用一次，具体原因暂时不知
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException, ClassNotFoundException {
        //在无法确定最后一个数据包的时候，可以使用标识符来区分
        try {
            ByteBuf bb = (ByteBuf) msg; // (1)
            byte[] q = new byte[bb.readableBytes()];
            bb.readBytes(q);
            array.add(q); // (1)
            if(q.length<10 ){
                System.out.println("channelRead 最后一个包");
            }else{
                byte[] tmp = new byte[10];
                for(int i=0;i<10;i++){
                    tmp[i]=q[q.length-11];
                }
                String endflag = new String(q);
                if(endflag.contains("NPCS##NPCS")){//最后一个包
                    byte[] b = this.byteMergerAll(this.array);
                    ByteArrayInputStream byteInt = new ByteArrayInputStream(b);
                    ObjectInputStream objInt = new ObjectInputStream(byteInt);
                    UploadFile uploadFileBean = null;
                    uploadFileBean = (UploadFile) objInt.readObject();
                    byte[] file_content = uploadFileBean.getB();
                    System.out.println(new String (file_content));
                    FileOutputStream fos = new FileOutputStream("D://test20191014.txt");
                    fos.write(file_content);;
                    System.out.println(uploadFileBean.getFileName());
                    exeReturn(ctx, "上传成功!", "0");
                    fos.close();
                    byteInt.close();
                    objInt.close();
                }
            }
        }finally {
            ctx.fireChannelRead(msg);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    private byte[] byteMergerAll(ArrayList<byte[]> values) {
        int length_byte = 0;
        for (int i = 0; i < values.size(); i++) {
            length_byte += values.get(i).length;
        }
        byte[] all_byte = new byte[length_byte-10];
        int countLength = 0;
        for (int i = 0;i < values.size(); i++) {
            byte[] b =values.get(i);
            if(countLength+b.length>length_byte-10){//计算是否为最后一个包
                //length_byte-10-countLength最后一个有效数据
                System.arraycopy(b, 0, all_byte, countLength, length_byte-10-countLength);
                break;
            }else {
                System.arraycopy(b, 0, all_byte, countLength, b.length);
            }
            countLength += b.length;
            b = null;
        }
        return all_byte;
    }
    private void exeReturn(ChannelHandlerContext ctx,String msg,String errorCode ) throws IOException {
        ResultBean resultBean = new ResultBean();
        resultBean.setMsg(msg);
        resultBean.setErrcode(errorCode);
        ByteArrayOutputStream baos =new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(resultBean);
        byte [] msg_byte = baos.toByteArray();
        baos.close();
        oos.close();
        final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer(msg_byte)); // (3)
//        f.addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                assert f == future;
//                ctx.close();
//            }
//        }); //
    }
}
