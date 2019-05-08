package com.filippov;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class PathHolder {

    public static final Path baseLocalPath = Paths.get("", "ClientStorage");
    private Path clientPath;
    private Path serverPath;
    private final HashMap <String, Path> clientPathMap = new HashMap<>();
    private final HashMap <String, Path> serverPathMap = new HashMap<>();

    public PathHolder() {
        this.clientPath = Paths.get("");
        this.serverPath = Paths.get("");
        System.out.println("Определение корневого пути: " + baseLocalPath.toAbsolutePath());
    }

    public Path getClientPath() {
        return clientPath;
    }

    public void setClientPath(Path clientPath) {
        this.clientPath = clientPath;
    }

    public Path getServerPath() {
        return serverPath;
    }

    public void setServerPath(Path serverPath) {
        this.serverPath = serverPath;
    }

    public HashMap<String, Path> getClientPathMap() {
        return clientPathMap;
    }

    public HashMap<String, Path> getServerPathMap() {
        return serverPathMap;
    }
}

