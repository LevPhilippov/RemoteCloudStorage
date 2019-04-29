package com.filippov;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.SortedMap;

public class PathHolder {

    private final String clientRootPath = "CloudStorageClientV2/Storage";
    private final String serverRootPath = "CloudStorageServer/Storage";
    private File clientPath;
    private File serverPath;
    private final HashMap <String, File> clientPathMap = new HashMap<>();
    private final HashMap <String, File> serverPathMap = new HashMap<>();

    public PathHolder() {
        this.clientPath = new File(clientRootPath);
    }

    public File getClientPath() {
        return clientPath;
    }

    public void setClientPath(File clientPath) {
        this.clientPath = clientPath;
    }

    public File getServerPath() {
        return serverPath;
    }

    public void setServerPath(File serverPath) {
        this.serverPath = serverPath;
    }

    public HashMap<String, File> getClientPathMap() {
        return clientPathMap;
    }

    public HashMap<String, File> getServerPathMap() {
        return serverPathMap;
    }
}

