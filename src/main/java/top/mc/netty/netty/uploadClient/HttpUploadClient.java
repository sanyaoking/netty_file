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
package top.mc.netty.netty.uploadClient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.internal.SocketUtils;
import top.mc.netty.util.HttpUtil;
import top.mc.netty.util.PropUtil;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class is meant to be run against {@link HttpUploadClient}.
 */
public final class HttpUploadClient {
    /**
     * 请求服务器地址
     */
    private final String BASE_URL = PropUtil.getV("baseUrlServer");
    private Bootstrap b = null;
    private String scheme = "";
    private String host = "";
    private int port = -1;
    private HttpDataFactory factory=null;
    private EventLoopGroup group = null;
    private URI uriSimple = null;
    public HttpUploadClient(){

    }
    public  static void main(String[] str){
        HttpUploadClient huc = new HttpUploadClient();
        huc.init();
        try {
            huc.upload("D://2018年度市级预算执行和其他财政收支审计工作报告.doc","20190924");
            huc.upload("D://孟超个人简历 .doc","20190924");
//            huc.delFile("20190924/2018年度市级预算执行和其他财政收支审计工作报告.doc");
//            huc.delFile("20190924/孟超个人简历 .doc");
            huc.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean init(){
        try {
            this.b = new Bootstrap();
            this.uriSimple  = new URI(this.BASE_URL);
            this.scheme =this. uriSimple.getScheme() == null? "http" : this.uriSimple.getScheme();
            this.host = this.uriSimple.getHost() == null? "127.0.0.1" : this.uriSimple.getHost();
            this.port = this.uriSimple.getPort();
            //如果没有设置端口则默认为80或443
            if (port == -1) {
                if ("http".equalsIgnoreCase(this.scheme)) {
                    port = 80;
                } else if ("https".equalsIgnoreCase(this.scheme)) {
                    port = 443;
                }
            }
            if (!"http".equalsIgnoreCase(this.scheme) && !"https".equalsIgnoreCase(this.scheme)) {
                throw new Exception("只支持http或https协议！");

            }
            final boolean ssl = "https".equalsIgnoreCase(this.scheme);
            final SslContext sslCtx;
            if (ssl) {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } else {
                sslCtx = null;
            }
            /*创建调度模块称*/
            this.group = new NioEventLoopGroup();
            // 设置工厂类，并配置对应的静态参数DiskFileUpload，DiskAttribute在DefaultHttpDataFactory中均有使用，所以也要配置，DefaultHttpDataFactory默认采用
            //MixedFileUpload 内存和文件同时使用限制的混合文件
            // DefaultHttpDataFactory(boolean useDisk) 这个方法可以采用DiskFileUpload上传
            this.factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if MINSIZE exceed
            DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
            DiskFileUpload.baseDirectory = null; // system temp directory
            DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on exit (in normal exit)
            DiskAttribute.baseDirectory = null; // system temp directory
            b.group(group).channel(NioSocketChannel.class).handler(new HttpUploadClientInitializer(sslCtx));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        } catch (SSLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("初始化成功！");
        return true;
    }

    /**
     *
     * @param file_str 本地文件路径
     * @param file_url 服务器文件保存路径
     * @throws Exception
     */
    public  void upload(String file_str,String file_url) throws Exception{
        File file = new File(file_str);
        if (!file.canRead()) {
            throw new FileNotFoundException(file_str);
        }
        this.upload(file,file_url);
    }
    /**
     *
     * @param file 文件流
     * @param file_url 服务器文件保存路径
     * @throws Exception
     */
    public  void upload(File file,String file_url) throws Exception{
        try {
            List<InterfaceHttpData> bodylist = this.formpostmultipart("POST","ADDFILE",file_url,file);
            if (bodylist == null) {
                factory.cleanAllHttpData();
                return;
            }
        } finally {
            factory.cleanAllHttpData();
        }
    }
    public  void delFile(String file_str) throws Exception{
        File file = new File(file_str);
        try {
            List<InterfaceHttpData> bodylist = this.formpost("POST","DELFILE", file.getParent(),file.getName());
            if (bodylist == null) {
                factory.cleanAllHttpData();
                return;
            }
        } finally {
            factory.cleanAllHttpData();
        }
    }
    public void shutdownGracefully(){
        this.group.shutdownGracefully();
    }

    /**
     *
     * @param method  请求的类型  get,post
     * @param optType 文件库操作  ADDFILE DELFILE
     * @param fileUrl 文件保存路径
     * @param file 具体文件路径
     * @return
     * @throws Exception
     */
    List<InterfaceHttpData> formpostmultipart(String method,String optType,String fileUrl,String file) throws Exception {
        File file_ = new File(file);
        return this.formpostmultipart(method,optType,fileUrl,file_);
    }
    /**
     *
     * @param method  请求的类型  get,post
     * @param optType 文件库操作  ADDFILE DELFILE
     * @param fileUrl 文件保存路径
     * @param file 具体文件
     * @return
     * @throws Exception
     */
    List<InterfaceHttpData> formpostmultipart(String method,String optType,String fileUrl,File file) throws Exception {
        // 创建request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());
        // 使用multipart请求
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(this.factory, request, true);  // false => not multipart  需要传输文件所以选择true
        // 添加
        bodyRequestEncoder.addBodyAttribute("getform", method);
        //文件操作类型
        bodyRequestEncoder.addBodyAttribute("optType",optType);
        bodyRequestEncoder.addBodyAttribute("fileUrl",fileUrl);
        //上传文件
        bodyRequestEncoder.addBodyFileUpload("myfile", file, "application/x-zip-compressed", false);
        return HttpUtil.formpostmultipart(this.b, this.host, this.port, this.uriSimple, request, bodyRequestEncoder);
    }

    /**
     *
     * @param method 请求的类型  get,post
     * @param optType 文件库操作  ADDFILE DELFILE
     * @param fileUrl 文件路径
     * @param fileName 删除文件的名称，当fileName为空的时候，删除整个目录
     * @return
     * @throws Exception
     */
    public List<InterfaceHttpData> formpost(String method,String optType,String fileUrl,String fileName) throws Exception {
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());
        //选择传输协议类型
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(this.factory, request, false);  // false => not multipart 不需要传输文件所以选择false
        bodyRequestEncoder.addBodyAttribute("getform", method);
        //文件操作类型
        bodyRequestEncoder.addBodyAttribute("optType",optType);
        bodyRequestEncoder.addBodyAttribute("fileUrl",fileUrl);
        bodyRequestEncoder.addBodyAttribute("fileName",fileName);
        return HttpUtil.formpost(this.b, this.host, this.port, this.uriSimple, request, bodyRequestEncoder);
    }
}
