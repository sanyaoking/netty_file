package top.mc.netty.netty.ByteClient;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import top.mc.netty.bean.UploadFile;

import java.io.*;
import java.net.SocketAddress;

/**
 * @author ：mengchao
 * @date ：Created in 2019/10/12 10:27
 * @description：文件流上传文件测试
 */
public class FileByteClient  extends ChannelOutboundHandlerAdapter{//也可以用这个ChannelInboundHandlerAdapter，在建立连接的时候，传输参数

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        this.uploadFile(ctx);
        super.read(ctx);
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("bind");

        super.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("connect");
        super.connect(ctx, remoteAddress, localAddress, promise);
//        this.uploadFile(ctx);
    }

    public FileByteClient() {
        super();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("disconnect");
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("close");
        super.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("deregister");
        super.deregister(ctx, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        System.out.println("flush");
        super.flush(ctx);
    }
    private String fileName;
    private FileInputStream fis;
    private String fileUrl;
    public FileByteClient(String fileName,FileInputStream fis,String fileUrl){
        super();
        this.fileName=fileName;
        this.fis=fis;
        this.fileUrl = fileUrl;
    }

    public void uploadFile(ChannelHandlerContext ctx) throws Exception {
        System.out.println("uploadFile");
//        FileInputStream fis = new FileInputStream("D://vw升级.sql");
        UploadFile uf = new UploadFile();
        byte[] b = new byte[1024];
        byte[] tmp = new byte[this.fis.available()];
        int i=0;
        while(this.fis.read(b)>=0){
            if((i * b.length+b.length)<=tmp.length) {
                System.arraycopy(b, 0, tmp, i * b.length, b.length);
            }else{
                System.arraycopy(b, 0, tmp, i * b.length, tmp.length-i * b.length);
            }
            i++;
            System.out.println(i+"============================================"+i);
        }
        uf.setB(tmp);
        uf.setFileName(fileName);
        uf.setDir(this.fileUrl );
        ByteArrayOutputStream baos =new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(uf);
        byte [] msg_byte = baos.toByteArray();
        //netty默认的传输类型Buffer，一定要注意个这个，否则会导致另一端走不到对应的channel
        ctx.write(Unpooled.copiedBuffer(msg_byte));
        String endflag = "NPCS##NPCS";
        byte[] endbyte = endflag.getBytes();//长度是个10
        System.out.println("endbyte length " + endbyte.length);
        ctx.writeAndFlush(Unpooled.copiedBuffer(endbyte));
        //如果使用这个则会调用下一个channel的ChannelRead
//        ctx.fireChannelRead(Unpooled.copiedBuffer(msg_byte));
//        super.read(ctx);
    }

}
