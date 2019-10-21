package top.mc.netty;

import top.mc.netty.netty.ByteClient.HttpUploadByteClient;
import top.mc.netty.netty.HttpFileStart;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ：mengchao
 * @date ：Created in 2019/10/21 9:46
 * @description：将新增或修改的文件提交到服务器
 */
public class ClientThread implements Runnable{
    private int cyc = 4;
    public ClientThread(int cyc){
        this.cyc = cyc;
    }

    @Override
    public void run() {
        System.out.println("===============================================================");
        this.time = System.currentTimeMillis();
        List<String> list = this.traverseFolder("E:/workspace/idea/npcs-parent/npcs-web/target/npcs-web-8.20-SNAPSHOT/upload");
        HttpUploadByteClient httpUploadByteClient = new HttpUploadByteClient();
        try {
            for(int i=0;i<list.size();i++) {
                File tempFile =new File(list.get(i).trim());
                String fileName = tempFile.getName();
                FileInputStream fis = new FileInputStream(tempFile);
                System.out.println("fileName = "+fileName);
                int index = tempFile.getPath().indexOf("upload");
                String path = tempFile.getPath().substring(index+7,tempFile.getPath().length()-fileName.length());
                httpUploadByteClient.uploadByte(fileName, fis, path);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            httpUploadByteClient.shutDown();
        }
    }
    private long time = 0L;
    public static void main(String[] str) throws Exception {
        //启动接受服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpFileStart.start(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //循环周期
        int cyc = 1;
        //初始化时间
        ClientThread ct = new ClientThread(cyc);
        Thread thread = new Thread(ct);
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        ses.scheduleAtFixedRate(thread, 0, cyc, TimeUnit.MINUTES);
    }

    public List<String> traverseFolder(String path) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<String> list = new ArrayList<String>();
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                System.out.println("文件夹是空的!");
                return null;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
//                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        list.addAll(traverseFolder(file2.getAbsolutePath()));
                    } else {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(file2.lastModified());
//                        System.out.println("文件:" + file2.getAbsolutePath()+";最后修改时间："+sdf.format(cal.getTime()));
                        if(this.time - file2.lastModified() <= this.cyc*60*1000){
                            list.add(file2.getAbsolutePath());
                            System.out.println("文件:" + file2.getAbsolutePath()+";最后修改时间："+sdf.format(cal.getTime()));
                        }
                    }
                }
                return list;
            }
        } else {
            System.out.println("文件不存在!");
            return null;
        }
    }
}
