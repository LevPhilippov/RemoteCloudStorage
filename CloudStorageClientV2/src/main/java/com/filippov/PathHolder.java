package com.filippov;


import java.io.File;

public class PathHolder {

    private final String clientRootPath = "CloudStorageClientV2/Storage";
    private final String serverRootPath = "CloudStorageServer/Storage";
    private String clientPath;
    private String serverPath;

    public PathHolder() {
        this.clientPath = clientRootPath;
        this.serverPath = serverRootPath;
//        System.out.println(targetPath);
//        System.out.println(clientRootPath);
    }

    public String getClientPath() {
        return clientPath;
    }

    public void setClientPath(String clientPath) {
        this.clientPath = clientPath;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }
}
