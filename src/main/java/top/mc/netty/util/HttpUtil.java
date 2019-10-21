package top.mc.netty.util;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.internal.SocketUtils;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/24 22:08
 * @description：客户端组织请求的工具
 */
public class HttpUtil {

    public static List<InterfaceHttpData> formpost(
            Bootstrap bootstrap,
            String host, int port, URI uriSimple, HttpRequest request,  HttpPostRequestEncoder bodyRequestEncoder) throws Exception {
        ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(host, port));
        Channel channel = future.sync().channel();
        //创建post请求22222
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, host);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP + "," + HttpHeaderValues.DEFLATE);
        headers.set(HttpHeaderNames.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.set(HttpHeaderNames.ACCEPT_LANGUAGE, "fr");
        headers.set(HttpHeaderNames.REFERER, uriSimple.toString());
        headers.set(HttpHeaderNames.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        headers.set(
//                HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(
//                        new DefaultCookie("my-cookie", "foo"),
//                        new DefaultCookie("another-cookie", "bar"))
//        );
        // finalize request
        request = bodyRequestEncoder.finalizeRequest();
        List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();
        // send request
        channel.write(request);
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();
        channel.closeFuture().sync();
        return bodylist;
    }

    /**
     *
     * @param bootstrap 启动类
     * @param host 文件服务器ip
     * @param port 文件服务器端口
     * @param uriSimple
     * @param request 请求类型
     * @param bodyRequestEncoder  存储了请求参数
     * @return
     * @throws Exception
     */
    public static List<InterfaceHttpData> formpostmultipart(
            Bootstrap bootstrap,
            String host, int port, URI uriSimple, HttpRequest request, HttpPostRequestEncoder bodyRequestEncoder) throws Exception {
        // Start the connection attempt.
        ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(host, port));
        // Wait until the connection attempt succeeds or fails.
        Channel channel = future.sync().channel();
        //添加header
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, host);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP + "," + HttpHeaderValues.DEFLATE);
        headers.set(HttpHeaderNames.ACCEPT_CHARSET, "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        headers.set(HttpHeaderNames.ACCEPT_LANGUAGE, "fr");
        headers.set(HttpHeaderNames.REFERER, uriSimple.toString());
        headers.set(HttpHeaderNames.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        headers.set(
                HttpHeaderNames.COOKIE, io.netty.handler.codec.http.cookie.ClientCookieEncoder.STRICT.encode(
                        new io.netty.handler.codec.http.cookie.DefaultCookie("my-cookie", "foo"),
                        new DefaultCookie("another-cookie", "bar"))
        );
        // 序列化请求
        request = bodyRequestEncoder.finalizeRequest();
        // Create the bodylist to be reused on the last version with Multipart support
        List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();
        //发送请求
        channel.write(request);
        if (bodyRequestEncoder.isChunked()) { // could do either request.isChunked()
            // either do it through ChunkedWriteHandler
            channel.write(bodyRequestEncoder);
        }
        channel.flush();
        channel.closeFuture().sync();
        return bodylist;
    }
}
