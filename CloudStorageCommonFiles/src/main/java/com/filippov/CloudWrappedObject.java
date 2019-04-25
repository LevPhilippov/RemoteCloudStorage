package com.filippov;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.nio.file.Path;

@Getter
@Setter
public class CloudWrappedObject implements Serializable {

    private TypeEnum typeEnum;
    private byte[] bytes;
    private long hashSum;
    private int parcelNumber;
    private int parcelsInObject;
    private String fileName;
    private String localPath;
    private String targetPath;
    private String msg;

    public CloudWrappedObject(TypeEnum typeEnum, byte[] bytes, String fileName, String localPath, String targetPath) {
        this.typeEnum = typeEnum;
        this.bytes = bytes;
        this.fileName = fileName;
        this.localPath = localPath;
        this.targetPath = targetPath;
    }

    public enum TypeEnum {
        FILE, SEPARATEDFILE
    }
}
