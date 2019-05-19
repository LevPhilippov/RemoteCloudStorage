package com.filippov;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class PathHolder {

    public static final Path baseLocalPath = Paths.get("", "ClientStorage");
    public static final Path baseServerPath = Paths.get("root");
    private Path clientPath;
    private Path serverPath;
    private final HashMap <String, Path> clientPathMap = new HashMap<>();
    private final HashMap <String, Path> serverPathMap = new HashMap<>();

    public PathHolder() {
        this.clientPath = Paths.get("");
        this.serverPath = baseServerPath;

        if (!Files.exists(baseLocalPath)) {
            try {
                Files.createDirectories(baseLocalPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        if(serverPath==null)
            this.serverPath = Paths.get("root");
        this.serverPath = serverPath;
    }

    public HashMap<String, Path> getClientPathMap() {
        return clientPathMap;
    }

    public HashMap<String, Path> getServerPathMap() {
        return serverPathMap;
    }
}

