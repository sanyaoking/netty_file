package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import top.mc.netty.util.FileUtil;
import top.mc.netty.util.ParaUtil;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 9:53
 * @description：用于解析参数
 */
public class ParaChannelHandler  extends SimpleChannelInboundHandler<HttpObject> {
    public HashMap<String,String> headers = null;
    public HashMap<String,String> cookies = null;
    public HashMap<String,String> urlParas = null;
    public HashMap<String,String> formaras = null;
    public HashMap<String,FileUpload> fileUploads = null;
    //由于分块传输，所以要用类属性来保存
    public HttpPostRequestDecoder decoder=null;

    /**
     * 默认为自动释放资源
     */
    public ParaChannelHandler(){
        super();
    }

    /**
     * autoRelease 为true时自动释放资源
     * @param autoRelease
     */
    public ParaChannelHandler(boolean autoRelease){
        super(autoRelease);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        //接收url参数，header和cookie,如果为post请求的话创建POST解码器
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)httpObject;
            this.headers = ParaUtil.getHeaders(httpObject);
            System.out.println("headers:"+this.headers);
            this.cookies = ParaUtil.getCookies(httpObject);
            System.out.println("cookies:"+this.cookies);
            this.urlParas = ParaUtil.getUrlParas(httpObject);
            System.out.println("urlParas:"+this.urlParas);
            if (HttpMethod.GET.equals(request.method())) {
                System.out.println("GET 请求不需要创建HttpPostRequestDecoder解码器");
                ctx.fireChannelRead(httpObject);
                return;
            }

            this.decoder = ((ReceiveChannelHandler)ctx.pipeline().get("receiveChannelHandler")).getDecoder();
        }
        if(this.decoder!=null){
            //判断是最后一块数据,统一处理form参数
            if (httpObject instanceof LastHttpContent) {
                HttpContent chunk = (HttpContent) httpObject;

                this.formaras = ParaUtil.getFormaras(this.decoder);
                System.out.println(ctx.channel().alloc().buffer().readableBytes());
                this.fileUploads = ParaUtil.getFileByListInterfaceHttpData(this.decoder.getBodyHttpDatas());
            }
        }
        ctx.fireChannelRead(httpObject);
    }
}
