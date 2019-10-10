package top.mc.netty.netty.busAtomHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.multipart.FileUpload;
import sun.rmi.runtime.Log;
import top.mc.netty.netty.handler.ParaChannelHandler;
import top.mc.netty.util.FileUtil;
import top.mc.netty.util.PropUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 10:36
 * @description：保存文件
 */
public class DelFileAtomHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ParaChannelHandler paraChannelHandler = (ParaChannelHandler)ctx.pipeline().get("paraChannelHandler");
        HashMap<String,String> formPara = paraChannelHandler.formaras;
        String baseUrl = PropUtil.getV("baseUrl");
        String fileUrl = formPara.get("fileUrl");
        String fileName = formPara.get("fileName");
        if(fileUrl==null){
            fileUrl="";
        }
        String file_str = baseUrl+File.separator+fileUrl+File.separator+fileName;
        File f = new File(file_str);
        boolean b = FileUtil.delFile(f);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove("delFileAtomHandler");
        System.out.println("channelInactive删除DelFileAtomHandler");
        super.channelInactive(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.pipeline().remove("delFileAtomHandler");
        System.out.println("channelInactive删除DelFileAtomHandler");
        super.exceptionCaught(ctx, cause);
    }
}
