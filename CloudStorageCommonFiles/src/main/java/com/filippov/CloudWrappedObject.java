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
    private Path pathAtTargetRepository;
    private String msg;

    public CloudWrappedObject(){
        this.typeEnum = TypeEnum.ECHO;
    }



}
