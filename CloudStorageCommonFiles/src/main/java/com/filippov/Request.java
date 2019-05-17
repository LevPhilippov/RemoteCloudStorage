package com.filippov;

import lombok.Getter;

import java.io.File;
import java.io.Serializable;
import java.util.List;

@Getter
public class Request implements Serializable {

    public enum RequestType {
        ANSWER, AUTH_SUCCESS, FILELIST, GETFILES, SENDFILES, DELETEFILES, CREATE_FOLDER, PROPERTY;
    }


    private RequestType requestType;
    private RequestType answerType;
    private File serverPath;
    private List<File> fileList;
    private String login;
    private FileProperties fileProperty;

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

    public Request setFileProperty(FileProperties fileProperty) {
        this.fileProperty = fileProperty;
        return this;
    }

    public Request setFileList (List<File> fileList) {
        this.fileList = fileList;
        return this;
    }

    //Login дописывается отдельно в AuthHandler на сервере.
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
