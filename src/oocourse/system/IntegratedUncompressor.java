/**
 * 
 */
package oocourse.system;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
  

public class IntegratedUncompressor {
    /**
     * 解压zip格式的压缩文件到当前文件夹，并重命名为zip的名字。 
     * @param zipFileName 
     * @throws IOException 
     * @throws RarException 
     * @throws Exception 
     */
    public synchronized void unzipFileThenRenameToZipName(String zipFileName) throws IOException, RarException {
        File file = new File(zipFileName);
        String strPath, gbkPath, strtemp;
        strPath = new File(file.getParent()).getAbsolutePath();  
        
        int count = 0;
        String name = file.getName();
        name = name.substring(0, name.indexOf('.'));
        
        ZipFile zipFile = new ZipFile(zipFileName);
        
        if((!file.exists()) && (file.length() <= 0)) {  
            throw new IOException();
        }

        @SuppressWarnings("rawtypes")
		java.util.Enumeration zipEntries = zipFile.getEntries();
        while(zipEntries.hasMoreElements()){  
            org.apache.tools.zip.ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();  
            gbkPath = zipEntry.getName();
            if (!zipEntry.isDirectory()){
                //读写文件  
                InputStream is = zipFile.getInputStream(zipEntry);  
                BufferedInputStream bis = new BufferedInputStream(is);  
                gbkPath = zipEntry.getName();
                //strtemp = strPath + "/" + gbkPath;
                int suffixIndex = gbkPath.indexOf('.');
                if (suffixIndex <= 0) {
                	continue;
                }
                while (gbkPath.indexOf('.', suffixIndex + 1) > suffixIndex) {
                	suffixIndex = gbkPath.indexOf('.', suffixIndex + 1);
                }
                if (!(gbkPath.substring(suffixIndex).equals(".java") || gbkPath.substring(suffixIndex).equals(".txt"))) {
                	continue;
                }
                strtemp = strPath + "/Extracted/" + name + '_' + count++ + gbkPath.substring(suffixIndex);
              
                FileOutputStream fos = new FileOutputStream(strtemp);  
                BufferedOutputStream bos = new BufferedOutputStream(fos);  
                int c;  
                while((c = bis.read()) != -1) {  
                    bos.write((byte) c);
                }  
                bos.close();  
                fos.close();  
            }  
        }
    }
    
    /** 
     * 解压zip格式的压缩文件到当前文件夹 
     * @param zipFileName 
     * @throws Exception 
     */  
    public synchronized void unzipFile(String zipFileName) throws Exception {  
        try {  
            File f = new File(zipFileName);  
            ZipFile zipFile = new ZipFile(zipFileName);  
            if((!f.exists()) && (f.length() <= 0)) {  
                throw new Exception("要解压的文件不存在!");  
            }  
            String strPath, gbkPath, strtemp;  
            File tempFile = new File(f.getParent());  
            strPath = tempFile.getAbsolutePath();  
            @SuppressWarnings("rawtypes")
			java.util.Enumeration e = zipFile.getEntries();  
            while(e.hasMoreElements()){  
                org.apache.tools.zip.ZipEntry zipEnt = (ZipEntry) e.nextElement();  
                gbkPath=zipEnt.getName();  
                if(zipEnt.isDirectory()){  
                    strtemp = strPath + "/" + gbkPath;  
                    File dir = new File(strtemp);  
                    dir.mkdirs();  
                    continue;  
                } else {  
                    //读写文件  
                    InputStream is = zipFile.getInputStream(zipEnt);  
                    BufferedInputStream bis = new BufferedInputStream(is);  
                    gbkPath=zipEnt.getName();  
                    strtemp = strPath + "/" + gbkPath;  
                  
                    //建目录  
                    String strsubdir = gbkPath;  
                    for(int i = 0; i < strsubdir.length(); i++) {  
                        if(strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {  
                            String temp = strPath + "/" + strsubdir.substring(0, i);  
                            File subdir = new File(temp);  
                            if(!subdir.exists())  
                            subdir.mkdir();  
                        }  
                    }  
                    FileOutputStream fos = new FileOutputStream(strtemp);  
                    BufferedOutputStream bos = new BufferedOutputStream(fos);  
                    int c;  
                    while((c = bis.read()) != -1) {  
                        bos.write((byte) c);  
                    }  
                    bos.close();  
                    fos.close();  
                }  
            }  
        } catch(Exception e) {  
            e.printStackTrace();  
            throw e;  
        }  
    }
      
    /** 
     * 解压zip格式的压缩文件到指定位置 
     * @param zipFileName 压缩文件 
     * @param extPlace 解压目录 
     * @throws Exception 
     */  
    public synchronized void unzip(String zipFileName, String extPlace) throws Exception {  
        try {  
            (new File(extPlace)).mkdirs();  
            File f = new File(zipFileName);  
            ZipFile zipFile = new ZipFile(zipFileName);  
            if((!f.exists()) && (f.length() <= 0)) {  
                throw new Exception("要解压的文件不存在!");  
            }  
            String strPath, gbkPath, strtemp;  
            File tempFile = new File(extPlace);  
            strPath = tempFile.getAbsolutePath();  
            @SuppressWarnings("rawtypes")
			java.util.Enumeration e = zipFile.getEntries();  
            while(e.hasMoreElements()){  
                org.apache.tools.zip.ZipEntry zipEnt = (ZipEntry) e.nextElement();  
                gbkPath=zipEnt.getName();  
                if(zipEnt.isDirectory()){  
                    strtemp = strPath + File.separator + gbkPath;  
                    File dir = new File(strtemp);  
                    dir.mkdirs();  
                    continue;  
                } else {  
                    //读写文件  
                    InputStream is = zipFile.getInputStream(zipEnt);  
                    BufferedInputStream bis = new BufferedInputStream(is);  
                    gbkPath=zipEnt.getName();  
                    strtemp = strPath + File.separator + gbkPath;  
                  
                    //建目录  
                    String strsubdir = gbkPath;  
                    for(int i = 0; i < strsubdir.length(); i++) {  
                        if(strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {  
                            String temp = strPath + File.separator + strsubdir.substring(0, i);  
                            File subdir = new File(temp);  
                            if(!subdir.exists())  
                            subdir.mkdir();  
                        }  
                    }  
                    FileOutputStream fos = new FileOutputStream(strtemp);  
                    BufferedOutputStream bos = new BufferedOutputStream(fos);  
                    int c;  
                    while((c = bis.read()) != -1) {  
                        bos.write((byte) c);  
                    }  
                    bos.close();  
                    fos.close();  
                }  
            }  
        } catch(Exception e) {  
            e.printStackTrace();  
            throw e;  
        }  
    }  
      
    /** 
     * 解压zip格式的压缩文件到指定位置 
     * @param zipFileName 压缩文件 
     * @param extPlace 解压目录 
     * @throws Exception 
     */  
    public synchronized void unzip(String zipFileName, String extPlace,boolean whether) throws Exception {  
        try {  
            (new File(extPlace)).mkdirs();  
            File f = new File(zipFileName);  
            ZipFile zipFile = new ZipFile(zipFileName);  
            if((!f.exists()) && (f.length() <= 0)) {  
                throw new Exception("要解压的文件不存在!");  
            }  
            String strPath, gbkPath, strtemp;  
            File tempFile = new File(extPlace);  
            strPath = tempFile.getAbsolutePath();  
            @SuppressWarnings("rawtypes")
			java.util.Enumeration e = zipFile.getEntries();  
            while(e.hasMoreElements()){  
                org.apache.tools.zip.ZipEntry zipEnt = (ZipEntry) e.nextElement();  
                gbkPath=zipEnt.getName();  
                if(zipEnt.isDirectory()){  
                    strtemp = strPath + File.separator + gbkPath;  
                    File dir = new File(strtemp);  
                    dir.mkdirs();  
                    continue;  
                } else {  
                    //读写文件  
                    InputStream is = zipFile.getInputStream(zipEnt);  
                    BufferedInputStream bis = new BufferedInputStream(is);  
                    gbkPath=zipEnt.getName();  
                    strtemp = strPath + File.separator + gbkPath;  
                  
                    //建目录  
                    String strsubdir = gbkPath;  
                    for(int i = 0; i < strsubdir.length(); i++) {  
                        if(strsubdir.substring(i, i + 1).equalsIgnoreCase("/")) {  
                            String temp = strPath + File.separator + strsubdir.substring(0, i);  
                            File subdir = new File(temp);  
                            if(!subdir.exists())  
                            subdir.mkdir();  
                        }  
                    }  
                    FileOutputStream fos = new FileOutputStream(strtemp);  
                    BufferedOutputStream bos = new BufferedOutputStream(fos);  
                    int c;  
                    while((c = bis.read()) != -1) {  
                        bos.write((byte) c);  
                    }  
                    bos.close();  
                    fos.close();  
                }  
            }  
        } catch(Exception e) {  
            e.printStackTrace();  
            throw e;  
        }  
    }
      
     /** 
     * 解压rar格式的压缩文件到指定目录下 
     * @param rarFileName 压缩文件 
     * @param extPlace 解压目录 
     * @throws IOException 
     * @throws RarException 
     * @throws Exception 
     */  
    public synchronized void unrar(String rarFileName, String extPlace) throws RarException, IOException {
    	File extFolder = new File(extPlace);
    	extFolder.mkdirs();
        File rarFile = new File(rarFileName);
        // 构建测解压缩类
        Archive archive = new Archive(rarFile, "", false);
        // 设置rar文件 
        List<FileHeader> fileHeaders = archive.getFileHeaders();
        String fileName = rarFile.getName();
        String suffix;
        fileName = fileName.substring(fileName.indexOf("1"), fileName.indexOf("1") + 8);
        new File(extPlace + "Extracted/").mkdirs();
        for (int i = 0; i < fileHeaders.size(); ++i) {
        	if (!fileHeaders.get(i).isDirectory()) {
            	suffix = fileHeaders.get(i).getFileNameString();
            	suffix = suffix.substring(suffix.indexOf('.'));
            	if (!(suffix.equals(".java") || suffix.equals(".txt"))) {
            		continue;
            	}
            	System.out.println(extFolder + fileName + "_" + i + suffix);
                FileOutputStream fileOutputStream = new FileOutputStream(new File(extPlace +
                		"Extracted/" + fileName + "_" + i + suffix));
                archive.extractFile(fileHeaders.get(i), fileOutputStream);
            	fileOutputStream.close();
        	}
        }
        archive.close();
    }
}  