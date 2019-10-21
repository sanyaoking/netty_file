/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.mc.netty.netty.ByteClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import top.mc.netty.util.PropUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is meant to be run against {@link HttpUploadByteClient}.
 */
public class HttpUploadByteClient {
    private   String host ="";
    private   int port = 0;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private  Bootstrap b = new Bootstrap(); // (1)
    {
        try {

            URI uriSimple  = new URI(PropUtil.getV("baseUrlServer"));
            this.host = uriSimple.getHost() == null? "127.0.0.1" : uriSimple.getHost();
            this.port = uriSimple.getPort();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        //初始化
        b.group(workerGroup); // (2)
        b.channel(NioSocketChannel.class); // (3)
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)

    }
    /**
     * 请求服务器地址
     */
    private final String BASE_URL = PropUtil.getV("baseUrlServer");
    public HttpUploadByteClient(){

    }
    public  static void main(String[] args) throws InterruptedException {
//        HttpUploadByteClient httpUploadByteClient = new HttpUploadByteClient();
//        try {
//            httpUploadByteClient.uploadByte();
//            httpUploadByteClient.uploadByte();
//        }finally {
//            httpUploadByteClient.shutDown();
//        }
        HttpUploadByteClient httpUploadByteClient = new HttpUploadByteClient();
        try {
            httpUploadByteClient.uploadByteFile("关于建立国务院向全国人大常委会报告国有资产管理情况制度的意见.html",new FileInputStream("D://关于建立国务院向全国人大常委会报告国有资产管理情况制度的意见.html"),"20191016");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void uploadByteFile( String fileName, FileInputStream fis,String fileUrl)throws InterruptedException {
        try {
           this.uploadByte(fileName,fis,fileUrl);
        }finally {
            this.shutDown();
        }
    }

    public  void uploadByte( String fileName, FileInputStream fis,String fileUrl ) throws InterruptedException {
            this.b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast("fileByteClient",new FileByteClient(fileName,fis,fileUrl )).addLast("fileClientHandler",new FileClientHandler());
                }
            });
            // Start the client.
            ChannelFuture f = this.b.connect(host, port).sync(); // (5)
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
    }
    public boolean isShutdown(){
        return this.workerGroup.isShutdown();
    }
    public void shutDown(){
        this.workerGroup.shutdownGracefully();
    }
}
