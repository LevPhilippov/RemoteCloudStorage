package com.filippov;


public class PathHolder {

    private final String rootPath = "CloudStorageClientV2/Storage/";
    private String clientPath;
    private String serverPath;

    public PathHolder() {
        this.clientPath = rootPath;
        this.serverPath = "root";
        System.out.println(clientPath);
        System.out.println(rootPath);
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
