package top.mc.netty.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import top.mc.netty.bean.ResultBean;
import top.mc.netty.netty.busAtomHandler.DelFileAtomHandler;
import top.mc.netty.netty.busAtomHandler.FileByteServerHandler;
import top.mc.netty.netty.busAtomHandler.HttpDownFileServerHandler;
import top.mc.netty.netty.busAtomHandler.SaveFileAtomHandler;
import top.mc.netty.netty.handler.ParaChannelHandler;
import top.mc.netty.netty.handler.ResourceHander;
import top.mc.netty.netty.handler.UploadEncoder;
import top.mc.netty.util.OPTFILE;

import java.util.ArrayList;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 11:12
 * @description：处理链接请求
 */
public class HttpServerByteDown extends SimpleChannelInboundHandler<HttpObject> {
    private static final AttributeKey<ResultBean> RESULTBEAN = AttributeKey.valueOf("resultBean");
    private String method = "";
    /**
     * 默认为自动释放资源
     */
    public HttpServerByteDown(){
        super();
    }

    /**
     * autoRelease 为true时自动释放资源
     * @param autoRelease
     */
    public HttpServerByteDown(boolean autoRelease){
        super(autoRelease);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        System.out.println(11111);
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)httpObject;
            this.method = request.method().name();
        }
        if (httpObject instanceof HttpContent) {
            HttpContent chunk = (HttpContent) httpObject;
            if("GET".equals(this.method)){//下载使用
                //接受完最后一个数据块之后进行处理
                System.out.println(chunk instanceof HttpObjectAggregator);
                System.out.println(chunk instanceof DefaultFullHttpRequest);
                if(chunk instanceof LastHttpContent) {//LastHttpContent   http请求
                    //由于文件下载使用response,所以需要添加response编码器 ,删除resourceHander
                    if (!httpObject.decoderResult().isSuccess()) {
                        AttributeKey<ArrayList<byte[]>> ARRAY_LIST_ATTRIBUTE_KEY = AttributeKey.valueOf("arrayListAttributeKey");
                        ArrayList<byte[]> arrayList = ctx.pipeline().channel().attr(ARRAY_LIST_ATTRIBUTE_KEY).get();
                        if (ctx.pipeline().get("fileByteServerHandler") == null) {
                            ctx.pipeline().remove("1");
                            ctx.pipeline().remove("2");
                            ctx.pipeline().remove("3");
                            ctx.pipeline().addLast( "fileByteServerHandler", new FileByteServerHandler(arrayList));
                            ctx.fireChannelRead(httpObject);
                            return;
                        }
                    }
                    if (ctx.pipeline().get("httpResponseEncoder") == null) {
                        ctx.pipeline().addAfter(ctx.name(), "httpResponseEncoder", new HttpResponseEncoder());
                    }
                    if (ctx.pipeline().get("chunkedWriteHandler") == null) {
                        ctx.pipeline().addAfter("httpResponseEncoder", "chunkedWriteHandler", new ChunkedWriteHandler());
                    }
                    if (ctx.pipeline().get("httpDownFileServerHandler") == null) {
                        ctx.pipeline().addAfter("chunkedWriteHandler", "httpDownFileServerHandler", new HttpDownFileServerHandler());
                    }
                    System.out.println("添加下载文件httpDownFileServerHandler");
                    ctx.fireChannelRead(httpObject);
                }/*else{
                    if (ctx.pipeline().get("fileByteServerHandler") == null) {
                        ctx.pipeline().remove("1");
                        ctx.pipeline().remove("2");
                        ctx.pipeline().remove("3");
                        AttributeKey<ArrayList<byte[]>> ARRAY_LIST_ATTRIBUTE_KEY = AttributeKey.valueOf("arrayListAttributeKey");
                        ArrayList<byte[]> arrayList = ctx.pipeline().channel().attr(ARRAY_LIST_ATTRIBUTE_KEY).get();
                        ctx.pipeline().addLast( "fileByteServerHandler", new FileByteServerHandler(arrayList));
                        ctx.fireChannelRead(httpObject);

                    }
                }*/


            }

        }
    }
}
