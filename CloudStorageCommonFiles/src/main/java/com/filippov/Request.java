package com.filippov;

import lombok.Getter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

@Getter
public class Request implements Serializable {

    public enum RequestType {
        ANSWER, AUTH, FILELIST, GETFILES, ECHO, DELETEFILES;
    }

    private RequestType requestType;
    private RequestType answerType;
    private File serverPath;
    private File clientPath;
    private List<File> fileList;

    public Request(RequestType requestType, RequestType answerType, File serverPath, List<File> fileList) {
        this.requestType = requestType;
        this.answerType = answerType;
        this.serverPath = serverPath;
        this.fileList = fileList;
    }

    public Request() {}

        public Request setRequestType(Request.RequestType requestType){
        this.requestType = requestType;
        return this;
    }

    public Request setAnswerType(Request.RequestType answerType) {
        this.answerType=answerType;
        return this;
    }

    public Request setServerPath(File serverPath) {
        this.serverPath = serverPath;
        return this;
    }
    public Request setClientPath(File clientPath) {
        this.clientPath = clientPath;
        return this;
    }

    public Request setFileList (List<File> fileList) {
        this.fileList = fileList;
        return this;
    }
}
