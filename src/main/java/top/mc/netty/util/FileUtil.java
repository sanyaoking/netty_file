package top.mc.netty.util;

import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;
import java.io.IOException;

public class FileUtil {

    public static boolean saveByFileUploadAndFileName(FileUpload fileUpload,String filestr){
        File file = new File(filestr);
        return FileUtil.saveByFileUploadAndFile(fileUpload,file);
    }

    public static boolean saveByFileUploadAndFile(FileUpload fileUpload,File file){
        boolean flag = true;
        if (fileUpload.isCompleted()) {
            try {
                fileUpload.renameTo(file);
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }else{
            flag = false;
        }
        return flag;
    }

    public static void createDir(String dirPath){
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }else{
            return;
        }
    }

    public static boolean delFile(String file){
        File f = new File(file);
        return FileUtil.delFile(f);
    }
    public static boolean delFile(File file){
        if(file.exists() && file.isFile()){
            file.delete();
            return true;
        }else{
            return false;
        }
    }
}
