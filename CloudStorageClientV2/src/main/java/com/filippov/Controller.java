package com.filippov;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.Getter;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable {

    Network network;

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
        //network binding
        network = Network.getInstance();
        network.setController(this);
        //listview setting
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setListenersOnListView();
        //refresh lists
        refreshLocalFilesList();
        network.requestFilesListFromServer();
    }

    private void setListenersOnListView() {
        localListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==2) {
                    File path = new File(network.getPathHolder().getClientPath().toString() + '/' + localListView.getSelectionModel().getSelectedItem());
                    if(path.isDirectory()) {
                        System.out.println("Новый путь к директории клиента: " + path.toString());
                        network.getPathHolder().setClientPath(path);

                        refreshLocalFilesList();
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
                    File path = new File(pathHolder.getServerPath().toString() + '/' + serverListView.getSelectionModel().getSelectedItem());
                    pathHolder.setServerPath(path);
                    System.out.println("Запрашиваю список файлов сервера в каталоге: " + path.toString());
                    Network.getInstance().requestFilesListFromServer();
                }
            }
        });
    }


    public void refreshLocalFilesList(){
        Runnable refresh = () -> {
            //обновление листа для клиента
            network.getPathHolder().getClientPathMap().clear();
            localListView.getItems().clear();
            Factory.giveFileList(network.getPathHolder().getClientPath()).forEach((path)->{
                network.getPathHolder().getClientPathMap().put(path.getName(),path);
            });
            localListView.getItems().setAll(network.getPathHolder().getClientPathMap().keySet());
            clientFolder.setText(network.getPathHolder().getClientPath().toString());
        };
        refreshPattern(refresh);

    }


    public void push() {
        ObservableList <String> os = localListView.getSelectionModel().getSelectedItems();
        Network.getInstance().writeFilesIntoChannel(os, Request.RequestType.GETFILES);
    }

    public void connect() {
        Network.setController(this);
    }

    public void disconnest() {
        Platform.runLater(() -> Network.getInstance().shutdown());
    }

    public void refreshServerFileList(List<File> serverFileList) {
        Runnable refresh = () -> {
            //обновление листа для сервера
            serverListView.getItems().clear();
            network.getPathHolder().getServerPathMap().clear();
            serverFileList.stream().forEach((path) -> {
                network.getPathHolder().getServerPathMap().put(path.getName(),path);

            });
            serverListView.getItems().setAll(network.getPathHolder().getServerPathMap().keySet());
            serverFolder.setText(network.getPathHolder().getServerPath().toString());
        };
        refreshPattern(refresh);
    }

    public void requestFile() {
        ObservableList<String> os = serverListView.getSelectionModel().getSelectedItems();
        network.sendFilesRequest(os, Request.RequestType.GETFILES);
    }

    public void stepBackServerPath(){
        network.getPathHolder().setServerPath(new File(network.getPathHolder().getServerPath().getParent()));
        network.requestFilesListFromServer();
    }

    public void stepBackClientPath(){
        network.getPathHolder().setClientPath(new File(network.getPathHolder().getClientPath().getParent()));
        refreshLocalFilesList();
    }


    private static void refreshPattern(Runnable refresh) {
        if (Platform.isFxApplicationThread()) {
            refresh.run();
        } else {
            Platform.runLater(refresh);
        }
    }

    public void delete() {
        ObservableList observableList = localListView.getSelectionModel().getSelectedItems();
        if(!observableList.isEmpty()) {
            System.out.println("Нажата кнопка удаления локальных файлов " + observableList);
            network.writeFilesIntoChannel(observableList, Request.RequestType.DELETEFILES);
        }
        observableList = serverListView.getSelectionModel().getSelectedItems();
        if (!observableList.isEmpty()) {
            System.out.println("Нажата кнопка удаления файлов на сервере " + observableList);
            network.sendFilesRequest(observableList, Request.RequestType.DELETEFILES);
        }
        refreshLocalFilesList();
        network.requestFilesListFromServer();
    }

    public void closeApp() {
        Platform.runLater(() -> Network.getInstance().shutdown());
        Platform.exit();
    }
}
