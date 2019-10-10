package top.mc.netty.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * @author ：mengchao
 * @date ：Created in 2019/9/19 10:43
 * @description：读取配置文件
 */
public class PropUtil {
    private static Properties properties = new Properties();
    private static HashMap<String,String> prop = new HashMap<String,String>();
    static{
        InputStream inputStream = Object.class.getResourceAsStream("/application.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PropUtil.init();
    }
    public static void init(){
        Set<Object> set = properties.keySet();
        String[] p = new String[set.size()];
        set.toArray(p);
        for(int i=0;i<p.length;i++){
            prop.put(p[i],properties.getProperty(p[i]));
        }
    }
    public static String getRealTimeByKey(String key){
        return properties.getProperty(key);
    }
    public static String getByKey(String key){
        return prop.get(key);
    }
    public static String getV(String key){
        if("0".equals(PropUtil.getByKey("debug"))){//debug模式取实时配置
            return PropUtil.getRealTimeByKey(key);
        }else{
            return PropUtil.getByKey(key);
        }
    }
}
