package top.mc.netty.util;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ParaUtil {
    public static HashMap<String,String> getHeaders(HttpObject httpObject){
        HashMap<String,String> headers = new HashMap<String,String>();
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            //获取参数
            for (Map.Entry<String, String> entry : request.headers()) {
                headers.put(entry.getKey(),entry.getValue());
            }
        }
        return headers;
    }

    public static HashMap<String,String> getCookies(HttpObject httpObject){
        HashMap<String,String> cookies = new HashMap<String,String>();
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            // new getMethod
            Set<Cookie> set_cookies;
            String value = request.headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                set_cookies = Collections.emptySet();
            } else {
                set_cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            for (Cookie cookie : set_cookies) {
                cookies.put(cookie.name(),cookie.value());
            }
        }
        return  cookies;
    }

    /**
     *
     * @param httpObject
     * @return
     */
    public static HashMap<String,String> getUrlParas(HttpObject httpObject){
        HashMap<String,String> urlParas = new HashMap<String,String>();
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Map.Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                int i = 0;
                for (String attrVal: attr.getValue()) {
                    if(i==0){
                        urlParas.put(attr.getKey(),attrVal);
                    }else {
                        urlParas.put(attr.getKey()+"_"+i, attrVal);
                    }
                    i=i+1;
                }
            }
        }
        return  urlParas;
    }

    /**
     * 由于存在分段传输的问题，所以一定要保证接收全部数据块之后在获取form参数
     * 流只能读取一次，所以当其他地方通过这种方法获取过form参数后，就不能再次获取
     * @param decoder
     * @return
     */
    public static HashMap<String,String> getFormaras(HttpPostRequestDecoder decoder){
        HashMap<String,String> formParas = new HashMap<String,String>();
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        String value;
                        try {
                            value = attribute.getValue();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            return formParas;
                        }
                        String tmp = formParas.get(attribute.getName());
                        if(tmp!=null){
                            formParas.put(attribute.getName(),tmp+","+attribute.getValue());
                        }else {
                            formParas.put(attribute.getName(), attribute.getValue());
                        }
                            /*if (value.length() > 100) {
                               //控制按数值长度
                            } else {

                            }*/
                    }
                    //文件类型保存文件名称
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        String tmp = formParas.get("fileName");
                        if(tmp!=null){
                            formParas.put("fileName", tmp+","+fileUpload.getName());
                        }else {
                            formParas.put("fileName", fileUpload.getName());
                        }
                    }
                }

            }
        }catch (Exception e){
            System.out.println("getFormaras 遍历参数完毕，抛出异常，但是官方提示不抛出异常！");
        }
        return  formParas;
    }
    /**
     * 由于存在分段传输的问题，所以一定要保证接收全部数据块之后在获取form参数
     * 流只能读取一次，所以当其他地方通过这种方法获取过form参数后，就不能再次获取
     * @param decoder
     * @return
     */
    public static HashMap<String,FileUpload> getFile(HttpPostRequestDecoder decoder){
        HashMap<String,FileUpload> formFile = new HashMap<String,FileUpload>();
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    //文件流处理
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        formFile.put(fileUpload.getFilename(),fileUpload);
                    }
                }

            }
        }catch (Exception e){
            System.out.println("getFile 遍历参数完毕，抛出异常，但是官方提示不抛出异常！");
        }
        return  formFile;
    }
    public static HashMap<String,FileUpload> getFileByListInterfaceHttpData( List<InterfaceHttpData> list){
        HashMap<String,FileUpload> formFile = new HashMap<String,FileUpload>();
        try {
            int i=0;
            while (list.size()>=i+1) {
                InterfaceHttpData data = list.get(i);
                if (data != null) {
                    //文件
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        formFile.put(fileUpload.getFilename(),fileUpload);
                    }
                }
                i++;
            }
        }catch (Exception e){
            System.out.println("getFile 遍历参数完毕，抛出异常，但是官方提示不抛出异常！");
        }
        return  formFile;
    }
    public static HashMap<String,FileUpload> getFileByInterfaceHttpData( InterfaceHttpData data){
        HashMap<String,FileUpload> formFile = new HashMap<String,FileUpload>();
        try {
                if (data != null) {
                    //文件流处理
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                        FileUpload fileUpload = (FileUpload) data;
                        formFile.put(fileUpload.getFilename(),fileUpload);
                    }
                }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("getFileByInterfaceHttpData");
        }
        return  formFile;
    }
}
