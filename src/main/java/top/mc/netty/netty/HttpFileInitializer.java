package top.mc.netty.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import top.mc.netty.netty.handler.*;

/**
* @author ：mengchao
* @date ：Created in 2019/9/19 9:53
* @description：绑定基础channelHandler
*/
public class HttpFileInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //绑定解码器
//        pipeline.addLast("1",new HttpRequestDecoder());
//        //HttpObjectAggregator 将多个消息转换为单一的一个FullHttpRequest，控制请求的大小
//        pipeline.addLast(new HttpObjectAggregator(65536*5));
//        //用于压缩数据(官方网站有这个东西，具体如何使用未知)
//        pipeline.addLast("3",new HttpContentCompressor());
//        //绑定接受分块数据HttpServer
//        pipeline.addLast("receiveChannelHandler",new ReceiveChannelHandler(false));
//        //绑定参数处理模块(用到上一个模块的属性)
//        pipeline.addLast("paraChannelHandler",new ParaChannelHandler(false));
//        //绑定参数处理模块(用到上一个模块的属性)
//        pipeline.addLast("httpServer",new HttpServer(false));
        //返回文件的channel

        pipeline.addLast("fileByteServerHandler",new FileByteServerHandler());
        //释放资源
//        pipeline.addLast("resourceHander",new ResourceHander());

//        pipeline.addLast("1",new HttpFileServer());
    }
}
