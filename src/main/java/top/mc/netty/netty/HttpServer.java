package top.mc.netty.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import top.mc.netty.bean.ResultBean;
import top.mc.netty.netty.busAtomHandler.DelFileAtomHandler;
import top.mc.netty.netty.busAtomHandler.SaveFileAtomHandler;
import top.mc.netty.netty.busAtomHandler.HttpDownFileServerHandler;
import top.mc.netty.netty.handler.*;
import top.mc.netty.util.OPTFILE;

import java.util.HashMap;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 11:12
 * @description：处理链接请求
 */
public class HttpServer extends SimpleChannelInboundHandler<HttpObject> {
    private static final AttributeKey<ResultBean> RESULTBEAN = AttributeKey.valueOf("resultBean");
    private String method = "";
    /**
     * 默认为自动释放资源
     */
    public HttpServer(){
        super();
    }

    /**
     * autoRelease 为true时自动释放资源
     * @param autoRelease
     */
    public HttpServer(boolean autoRelease){
        super(autoRelease);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)httpObject;
            this.method = request.method().name();
        }
        if (httpObject instanceof HttpContent) {
            ParaChannelHandler paraChannelHandler = (ParaChannelHandler) ctx.pipeline().get("paraChannelHandler");
            HashMap<String, String> formPara = paraChannelHandler.formaras;
            HashMap<String, String> header = paraChannelHandler.headers;
            HashMap<String, String> urlParas = paraChannelHandler.urlParas;
            String type = urlParas.get("type");
            if("GET".equals(this.method) && type ==null){//下载使用
                HttpContent chunk = (HttpContent) httpObject;
                //接受完最后一个数据块之后进行处理
                if(chunk instanceof LastHttpContent) {
                    //由于文件下载使用response,所以需要添加response编码器 ,删除resourceHander
                    if (ctx.pipeline().get("httpResponseEncoder") == null) {
                        ctx.pipeline().addAfter(ctx.name(), "httpResponseEncoder", new HttpResponseEncoder());
                    }
                    if (ctx.pipeline().get("chunkedWriteHandler") == null) {
                        ctx.pipeline().addAfter("httpResponseEncoder", "chunkedWriteHandler", new ChunkedWriteHandler());
                    }
                    if (ctx.pipeline().get("httpDownFileServerHandler") == null) {
                        ctx.pipeline().addAfter("chunkedWriteHandler", "httpDownFileServerHandler", new HttpDownFileServerHandler());
                    }
                }
                System.out.println("添加下载文件httpDownFileServerHandler");
            }else if("byte".equals(type)) {//通过byte或者文件流上传文件使用
                //添加处理器
                ctx.pipeline().addAfter("uploadDecoder", "fileByteServerHandler", new FileByteServerHandler());
                //添加编码器
                ctx.pipeline().addAfter("uploadDecoder", "uploadEncoder", new UploadEncoder());
            }else{//http上传文件使用
                HttpContent chunk = (HttpContent) httpObject;
                //接受完最后一个数据块之后进行处理
                if(chunk instanceof LastHttpContent) {
                    ctx.pipeline().addAfter("httpServer","resourceHander",new ResourceHander());
                    String optType = formPara.get("optType");
                    ResultBean resultBean = new ResultBean();
                    //根据不同的路径添加删除不同的channelHandler
                    if (OPTFILE.DELFILE.getoptType().equals(optType)) {
                        resultBean.setOptType("DELFILE");
                        ctx.pipeline().addAfter("httpServer", "delFileAtomHandler", new DelFileAtomHandler());
                        System.out.println("添加删除文件channelHandler");
                    } else if (OPTFILE.ADDFILE.getoptType().equals(optType)) {
                        resultBean.setOptType("ADDFILE");
                        ctx.pipeline().addAfter("httpServer", "saveFileAtomHandler", new SaveFileAtomHandler());
                        System.out.println("添加保存文件channelHandler");
                    } else if (OPTFILE.COVERFILE.getoptType().equals(optType)) {
                        resultBean.setOptType("COVERFILE");
                    } else {
                        resultBean.setOptType("optType");
                        throw new Exception("不存在的操作类型!");
                    }
                    ctx.pipeline().addLast("uploadEncoder",new UploadEncoder());
                    ctx.channel().attr(RESULTBEAN).setIfAbsent(resultBean);
                }

            }
            ctx.fireChannelRead(httpObject);
        }
    }
}
