package top.mc.netty.netty.busAtomHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.util.AttributeKey;
import top.mc.netty.bean.ResultBean;
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
public class SaveFileAtomHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ParaChannelHandler paraChannelHandler = (ParaChannelHandler)ctx.pipeline().get("paraChannelHandler");
        HashMap<String,FileUpload> fileUploads = paraChannelHandler.fileUploads;
        HashMap<String,String> formPara = paraChannelHandler.formaras;
        Set<String> fileuplad = fileUploads.keySet();
        String[] f = new String[fileuplad.size()];
        fileuplad.toArray(f);
        String baseUrl = PropUtil.getV("baseUrl");
        String fileUrl = formPara.get("fileUrl");
        if(fileUrl==null){
            fileUrl="";
        }
        for(int i=0;i<f.length;i++){
            FileUtil.createDir(baseUrl+File.separator+fileUrl);
            FileUtil.saveByFileUploadAndFileName(fileUploads.get(f[i]),baseUrl+File.separator+fileUrl+File.separator+f[i]);
        }
        AttributeKey<ResultBean> reusltBean = AttributeKey.valueOf("resultBean");
        ResultBean resultBean = ctx.channel().attr(reusltBean).get();
        resultBean.setErrcode("0");
        resultBean.setMsg("保存成功！");
        ctx.fireChannelRead(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove("saveFileAtomHandler");
        System.out.println("channelInactive删除SaveFileAtomHandler");
        super.channelInactive(ctx);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.pipeline().remove("saveFileAtomHandler");
        System.out.println("exceptionCaught删除SaveFileAtomHandler");
        super.exceptionCaught(ctx, cause);
    }
}
