package com.filippov;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class Request implements Serializable {

    public enum Requests {
        ANSWER, AUTH, FILELIST, GETFILES, ECHO, DELETEFILES;
    }

    private Request.Requests requestType;
    private String path;
    private List<String> fileList;
}
