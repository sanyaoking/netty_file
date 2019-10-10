package top.mc.netty.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import top.mc.netty.util.FileUtil;
import top.mc.netty.util.ParaUtil;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;

public class HttpFileServer extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger logger = Logger.getLogger(HttpFileServer.class.getName());
    private HttpPostRequestDecoder decoder;
    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed
    private HttpData partialContent=null;
    private HttpRequest request;
    private HashMap<String,String> headers = null;
    private HashMap<String,String> cookies = null;
    private HashMap<String,String> urlParas = null;
    private HashMap<String,String> formaras = null;
    private HashMap<String,FileUpload> fileUploads = null;
    //连接断开时，关闭资源
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        //接收url参数，header和cookie,如果为post请求的话创建POST解码器
        if (httpObject instanceof HttpRequest) {
            this.request = (HttpRequest)httpObject;
            this.headers = ParaUtil.getHeaders(httpObject);
            System.out.println("headers:"+this.headers);
            this.cookies = ParaUtil.getCookies(httpObject);
            System.out.println("cookies:"+this.cookies);
            this.urlParas = ParaUtil.getUrlParas(httpObject);
            System.out.println("urlParas:"+this.urlParas);
            if (HttpMethod.GET.equals(request.method())) {
                System.out.println("GET 请求不需要创建HttpPostRequestDecoder解码器");
                return;
            }
            try {
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                e1.printStackTrace();
                writeResponse(ctx.channel(), true);
                return;
            }
            //是否为分块传出
            boolean readingChunks = HttpUtil.isTransferEncodingChunked(request);
        }

        //如果是post请求，利用HttpPostRequestDecoder解析器解析参数
        if (decoder != null) {
            if (httpObject instanceof HttpContent) {
                // 接受新的分块数据
                HttpContent chunk = (HttpContent) httpObject;
                try {
                    decoder.offer(chunk);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    reset();
                    writeResponse(ctx.channel(), true);
                    return;
                }
                //判断是最后一块数据
                if (chunk instanceof LastHttpContent) {
                    this.formaras = ParaUtil.getFormaras(this.decoder);
                    this.fileUploads = ParaUtil.getFileByListInterfaceHttpData(this.decoder.getBodyHttpDatas());
                    Set<String> fileuplad = this.fileUploads.keySet();
                    String[] f = new String[fileuplad.size()];
                    fileuplad.toArray(f);
                    for(int i=0;i<f.length;i++){
                        FileUtil.saveByFileUploadAndFileName(this.fileUploads.get(f[i]),"d://qq.txt");
                    }
                    writeResponse(ctx.channel());
                    reset();
                }
            }
        } else {
            writeResponse(ctx.channel());
        }
    }
    private void reset() {
        this.request = null;
        this.decoder.destroy();
        this.decoder = null;
    }

    private void writeResponse(Channel channel) {
        writeResponse(channel, false);
    }

    private void writeResponse(Channel channel, boolean forceClose) {
        // Convert the response content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer("writeResponse", CharsetUtil.UTF_8);
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request) && !forceClose;

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        Set<Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 读取request分块数据
     */
    private void readHttpDataChunkByChunk() {

        try {
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {

                        builder.append("Start FileUpload: ")
                                .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        try {
                            System.out.println("Attribute:"+partialContent.getName()+",value:"+partialContent.getString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    System.out.println(builder.toString());
                    logger.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    System.out.println(builder.toString());
                    logger.info(builder.toString());
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end
            //e1.printStackTrace();
            //官方推荐 出现次异常说明读取数据结束
            return;
        }
    }
    private void saveFile(String path,HttpPostRequestDecoder decoder) throws IOException {
        File file = new File(path);
        OutputStream out = new FileOutputStream(file);
        try {
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        out.write(partialContent.toString().getBytes());
                        out.flush();
                        out.close();
                        builder.append("Start FileUpload: ")
                                .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        try {
                            System.out.println("Attribute:"+partialContent.getName()+",value:"+partialContent.getString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    System.out.println(builder.toString());
                    logger.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    System.out.println(builder.toString());
                    logger.info(builder.toString());
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
            // end
            //e1.printStackTrace();
            //官方推荐 出现磁异常说明读取数据结束
            return;
        }
    }
    private void writeHttpData(InterfaceHttpData data) {
        System.out.println(data.getHttpDataType());
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                // Error while reading data from File, only print name and error
                e1.printStackTrace();
                System.out.println("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                        + attribute.getName() + " Error while reading value: " + e1.getMessage() + "\r\n");
                return;
            }
            if (value.length() > 100) {
                System.out.println("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                        + attribute.getName() + " data too long\r\n");
            } else {
                System.out.println("\r\nBODY Attribute: " + attribute.getHttpDataType().name() + ": "
                        + attribute + "\r\n");
            }
        } else {
            System.out.println("\r\nBODY FileUpload: " + data.getHttpDataType().name() + ": " + data
                    + "\r\n");
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    final File file = new File("d;//qq.txt");

                    try {
                        FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                        FileChannel outputChannel = new FileOutputStream(file).getChannel();
                        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (fileUpload.length() < 1000000) {
                        System.out.println("\tContent of file\r\n");
                        try {
                            System.out.println(fileUpload.getString(fileUpload.getCharset()));
                        } catch (IOException e1) {
                            // do nothing for the example
                            e1.printStackTrace();
                        }
                        System.out.println("\r\n");
                    } else {
                        System.out.println("\tFile too long to be printed out:" + fileUpload.length() + "\r\n");
                    }
                } else {
                     System.out.println("\tFile to be continued but should not!\r\n");
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("===================exceptionCaught======================");
        ByteBuf buf = copiedBuffer("writeResponse", CharsetUtil.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }
}
