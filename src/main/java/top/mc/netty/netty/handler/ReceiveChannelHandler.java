package top.mc.netty.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import top.mc.netty.util.FileUtil;
import top.mc.netty.util.ParaUtil;

import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 9:53
 * @description：用于接受数据
 */
public class ReceiveChannelHandler extends SimpleChannelInboundHandler<HttpObject> {
    private HttpPostRequestDecoder decoder;
    private HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
    private ByteBuf buffer=null;
    /**
     * 默认为自动释放资源
     */
    public ReceiveChannelHandler(){
        super();
    }

    /**
     * autoRelease 为true时自动释放资源
     * @param autoRelease
     */
    public ReceiveChannelHandler(boolean autoRelease){
        super(autoRelease);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject httpObject) throws Exception {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if (HttpMethod.POST.equals(request.method())||HttpMethod.OPTIONS.equals(request.method())) {
                System.out.println("POST OPTIONS 请求需要创建HttpPostRequestDecoder解码器");
                try {
                    decoder = new HttpPostRequestDecoder(factory, request);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    throw new Exception("ReceiveChannelHandler创建HttpPostRequestDecoder解码器异常！");
                }
            }
        }
        if(this.decoder!=null) {
            if (httpObject instanceof HttpContent) {
                // 接受新的分块数据
                HttpContent chunk = (HttpContent) httpObject;
                try {
                    decoder.offer(chunk);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    throw new Exception("ReceiveChannelHandler接收分块数据异常！");
                }
            }
        }
        channelHandlerContext.fireChannelRead(httpObject);
    }

    public HttpPostRequestDecoder getDecoder(){
        return this.decoder;
    }
}
