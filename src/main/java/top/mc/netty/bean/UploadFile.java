package top.mc.netty.bean;

import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.Serializable;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/27 14:12
 * @description：文件上传bean
 */
public class UploadFile  implements Serializable {
    private byte[] b;
    private String fileName;
    private String dir;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public UploadFile(){
    }

    public UploadFile(String fileName){
        this.fileName = fileName;
    }

    public UploadFile(int length,String fileName){
        b = new byte[length];
        this.fileName = fileName;
    }

    public byte[] getB() {
        return b;
    }

    public void setB(byte[] b) {
        this.b = b;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
