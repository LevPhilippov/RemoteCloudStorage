package com.filippov;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import sun.nio.ch.Net;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable {

    @FXML
    private ListView serverListView;

    @FXML
    private ListView localListView;

    @FXML
    private TextField serverFolder;

    @FXML
    private TextField clientFolder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Class.forName("com.filippov.Network");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshLocalFileList(Network.getInstance().getPathHolder().getClientPath());
        setListenersOnListView();
    }

    private void setListenersOnListView() {
        localListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==2) {
                    PathHolder pathHolder = Network.getInstance().getPathHolder();
                    String path = pathHolder.getClientPath() + '/' + (String)localListView.getSelectionModel().getSelectedItem();
                    if(Files.isDirectory(Paths.get(path))) {
                        System.out.println("Новый путь к директории клиента: " + path);
                        refreshLocalFileList(path);
                        return;
                    }
                    System.out.println("Выбранный файл не является директорией!");
                }
            }
        });

        serverListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==2) {
                    PathHolder pathHolder = Network.getInstance().getPathHolder();
                    String path = pathHolder.getServerPath() + '/' + (String)serverListView.getSelectionModel().getSelectedItem();
                    System.out.println("Запрашиваю список файлов сервера в каталоге: " + path);
                    Network.getInstance().requestFilesList(path);
                }
            }
        });
    }

    public void refreshLocalFileList(String path) {
        if (Platform.isFxApplicationThread()) {
            Network.getInstance().getPathHolder().setClientPath(path);
            localListView.getItems().clear();
            Factory.giveFileList(path).stream().forEach(localListView.getItems()::add);
        } else {
            Platform.runLater(() -> {
                Network.getInstance().getPathHolder().setClientPath(path);
                localListView.getItems().clear();
                Factory.giveFileList(path).stream().forEach(localListView.getItems()::add);
            });
        }
        clientFolder.setText(Network.getInstance().getPathHolder().getClientPath());
    }

    public void synchronize() {
        ObservableList <String> os = localListView.getSelectionModel().getSelectedItems();
        Network.getInstance().sendFileToCloud(os);
    }

    public void connect() {
        Network.setController(this);
        Network.getInstance().startNetwork();
    }

    public void disconnest() {
        Platform.runLater(() -> Network.getInstance().shutdown());
        Platform.exit();
    }

    public void refreshServerFileList(List<String> serverFileList) {
        if (Platform.isFxApplicationThread()) {
                serverListView.getItems().clear();
                serverFileList.stream().forEach(serverListView.getItems()::add);
                serverFolder.setText(Network.getInstance().getPathHolder().getServerPath());

        } else {
            Platform.runLater(() -> {
                    serverListView.getItems().clear();
                    serverFileList.stream().forEach(serverListView.getItems()::add);
                    serverFolder.setText(Network.getInstance().getPathHolder().getServerPath());
            });
        }

    }

    public void requestFile() {
        ObservableList<String> os = serverListView.getSelectionModel().getSelectedItems();
        Network.getInstance().requestFile(os);
    }

    public void stepBackServerPath(){
        String newPath = Factory.giveStepBackPath(Network.getInstance().getPathHolder().getServerPath());
        Network.getInstance().requestFilesList(newPath);
    }

    public void stepBackClientPath(){
        String newPath = Factory.giveStepBackPath(Network.getInstance().getPathHolder().getClientPath());
        refreshLocalFileList(newPath);
    }




}
