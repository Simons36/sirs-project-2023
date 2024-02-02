package GrooveServer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UtilClasses {
    
    public static void WriteToFile(byte[] content, String path) throws FileNotFoundException, IOException{
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
        System.out.println("Wrote " + content.length + " bytes to file " + path);
    }

    public static byte[] ReadFromFile(String path) throws FileNotFoundException, IOException{
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static String encodeByteToBase64(byte[] bytes){
        return java.util.Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBase64ToByte(String base64){
        return java.util.Base64.getDecoder().decode(base64);
    }

    public static void RenameFile(String oldPath, String newPath){

        File oldFile = new File(oldPath);
        File newFile = new File(newPath);

        oldFile.renameTo(newFile);

        
    }
}
