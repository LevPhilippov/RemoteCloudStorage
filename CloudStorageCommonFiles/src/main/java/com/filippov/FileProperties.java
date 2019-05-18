package com.filippov;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class FileProperties implements Serializable {
    String fileName;
    String filePath;
    String size;
    String lastModificationTime;

    public FileProperties(String fileName, String filePath, Path path) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.size = getSize(path);
        this.lastModificationTime = getLastModificationTime(path);
    }

    private String getSize(Path path) {
        String result = null;
        if(Files.exists(path)) {
            try {
                Long fileSize = Files.size(path);
                if(fileSize<1024) {
                    result = Long.toString(fileSize) + "bytes";
                }
                else if (fileSize>1024 && fileSize<1024*1024) {
                    result = Long.toString(fileSize/1024) + "Kbytes";
                }
                else
                    {
                        Double doubleFileSize = Double.longBitsToDouble(fileSize);
                        result  = Double.toString(doubleFileSize/(1024*1024)) + "Mbytes";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String getLastModificationTime(Path path) {
        String time=null;
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            String pattern = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            return simpleDateFormat.format(new Date(fileTime.toMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return time;
    }
}
