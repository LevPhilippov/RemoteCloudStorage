package com.filippov;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

@Getter
@Setter
public class WrappedFile implements Serializable {

    private TypeEnum typeEnum;
    private byte[] bytes;
    private long chunkNumber;
    private long chunkslsInFile;
    private String fileName;
    private File localPath;
    private File targetPath;
    //Не передавать login по каналу! Использовать только на сервере! Дописывать в AuthHandlere-e.
    private String login;

    public enum TypeEnum {
        FILE, CHUNKED
    }

    public WrappedFile(TypeEnum typeEnum, byte[] bytes, long chunkNumber, long chunkslsInFile, String fileName, File targetPath) {
        this.typeEnum = typeEnum;
        this.bytes = bytes;
        this.chunkNumber = chunkNumber;
        this.chunkslsInFile = chunkslsInFile;
        this.fileName = fileName;
        this.targetPath = targetPath;
    }
//builders
    public WrappedFile setTypeEnum(TypeEnum typeEnum){
        this.typeEnum = typeEnum;
        return this;
    }
    public WrappedFile setBytes(byte[] bytes){
        this.bytes = bytes;
        return this;
    }
    public WrappedFile setFileName(String fileName){
        this.fileName = fileName;
        return this;
    }
    public WrappedFile setLocalPath(File localPath){
        this.localPath = localPath;
        return this;
    }
    public WrappedFile setTargetPath(File targetPath){
        this.targetPath = targetPath;
        return this;
    }
    public WrappedFile setChunkNumber(long chunkNumber){
        this.chunkNumber = chunkNumber;
        return this;
    }
    public WrappedFile setChunksInFile(long chunksInFile) {
        this.chunkslsInFile = chunksInFile;
        return this;
    }

    public WrappedFile setLogin(String login) {
        this.login = login;
        return this;
    }
}
