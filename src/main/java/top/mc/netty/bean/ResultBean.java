package top.mc.netty.bean;

import java.io.Serializable;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/20 9:51
 * @description：返回信息
 */
public class ResultBean implements Serializable {
    private String msg = "";
    private String optType = "";
    private String errcode="";
    private String data="";

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" +
                "msg:'" + msg + '\'' +
                ", optType:'" + optType + '\'' +
                ", errcode:'" + errcode + '\'' +
                ", data:'" + data + '\'' +
                '}';
    }
}
