package top.mc.netty.util;

public enum OPTFILE {
    DELFILE("DELFILE"),ADDFILE("ADDFILE"),COVERFILE("COVERFILE");
    private String optType;
    // 构造方法
    private OPTFILE(String optType) {
        this.optType = optType;
    }
    /**
     * 获取支付状态对象对应的中文名称
     * @return
     */
    public String getoptType() {
        return this.optType;
    }
}
