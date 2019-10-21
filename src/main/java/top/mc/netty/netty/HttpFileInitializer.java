package top.mc.netty.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import top.mc.netty.netty.busAtomHandler.FileByteServerHandler;
import top.mc.netty.netty.handler.*;
import top.mc.netty.util.PropUtil;

/**
* @author ：mengchao
* @date ：Created in 2019/9/19 9:53
* @description：绑定基础channelHandler
*/
public class HttpFileInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        if("1".equals(PropUtil.getV("isByteFile"))){
            //正常的文件下载
            this.initChannelFile(socketChannel);
        }else{
            this.initChannelByte(socketChannel);
        }
    }
    private void initChannelByte(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //绑定解码器
        pipeline.addLast("httpRequestDecoderByteMC",new HttpRequestDecoderByteMC());
    }
    private void initChannelFile(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //绑定解码器 经过HttpServerCodec解码之后，一个HTTP请求会导致：ParseRequestHandler的 channelRead()方法调用多次（测试时 "received message"输出了两次）
        pipeline.addLast("1",new HttpRequestDecoder());
        //HttpObjectAggregator 将多个消息转换为单一的一个FullHttpRequest，控制请求的大小  将多个消息转换为单一的一个FullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(65536*5));
//        //用于压缩数据(官方网站有这个东西，具体如何使用未知)
        pipeline.addLast("3",new HttpContentCompressor());
        //绑定接受分块数据HttpServer
        pipeline.addLast("receiveChannelHandler",new ReceiveChannelHandler(false));
//        //绑定参数处理模块(用到上一个模块的属性)
        pipeline.addLast("paraChannelHandler",new ParaChannelHandler(false));
//        //绑定参数处理模块(用到上一个模块的属性)
        pipeline.addLast("httpServer",new HttpServer(false));
        //返回文件的channel
//        pipeline.addLast(new StringDecoder());
//        pipeline.addLast("fileByteServerHandler",new FileByteServerHandler());?
        //释放资源
//        pipeline.addLast("resourceHander",new ResourceHander());

//        pipeline.addLast("1",new HttpFileServer());
    }
}
