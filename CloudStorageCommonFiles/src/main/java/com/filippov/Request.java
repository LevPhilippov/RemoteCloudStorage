package com.filippov;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
public class Request implements Serializable {

    public enum RequestType {
        ANSWER, AUTH, FILELIST, GETFILES, ECHO, DELETEFILES;
    }

    private RequestType requestType;
    private RequestType answerType;
    private String path;
    private List<String> fileList;

    public Request setRequestType(Request.RequestType requestType){
        this.requestType = requestType;
        return this;
    }

    public Request setAnswerType(Request.RequestType answerType) {
        this.answerType=answerType;
        return this;
    }

    public Request setPath(String path) {
        this.path = path;
        return this;
    }

    public Request setFileList (List<String> fileList) {
        this.fileList = fileList;
        return this;
    }
}
