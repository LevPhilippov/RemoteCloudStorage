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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable {

    public static Controller controller;

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
        controller = this;
        //network binding
        network = Network.getInstance();
//        network.setController(this);
        //listview setting
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setListenersOnListView();
        //refresh lists
        refreshLocalFilesList();
        network.requestFilesListFromServer(null);
    }

    private void setListenersOnListView() {
        localListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==1) {
                    serverListView.getSelectionModel().clearSelection();
                }
                if(event.getClickCount()==2) {
                    Path path = Paths.get(PathHolder.baseLocalPath.toString(), network.getPathHolder().getClientPath().toString(), (String)localListView.getSelectionModel().getSelectedItem());
                    System.out.println(path.toString());
                    if(Files.isDirectory(path)) {
                        System.out.println("-----------------------------");
                        System.out.println("Новый путь к директории клиента: " + path.toString());
                        System.out.println("-----------------------------");
                        network.getPathHolder().setClientPath(PathHolder.baseLocalPath.relativize(path));
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
                if(event.getClickCount()==1) {
                    localListView.getSelectionModel().clearSelection();
                }
                if(event.getClickCount()==2) {
                    Path path = Paths.get(network.getPathHolder().getServerPath().toString(),(String)serverListView.getSelectionModel().getSelectedItem());
//                    network.getPathHolder().setTargetPath(path);
                    System.out.println("Запрашиваю список файлов сервера в каталоге: " + path.toString());
                    Network.getInstance().requestFilesListFromServer(path);
                }
            }
        });
    }


    public void refreshLocalFilesList(){
        Runnable refresh = () -> {
            //обновление листа для клиента
            network.getPathHolder().getClientPathMap().clear();
            localListView.getItems().clear();
            Factory.giveFileList(Paths.get(PathHolder.baseLocalPath.toString(), network.getPathHolder().getClientPath().toString())).forEach((path)->{
                network.getPathHolder().getClientPathMap().put(path.getFileName().toString(),path);
            });
            localListView.getItems().setAll(network.getPathHolder().getClientPathMap().keySet());
            clientFolder.setText(network.getPathHolder().getClientPath().toString());
        };
        refreshPattern(refresh);
    }


    public void refreshServerFileList(List<File> serverFileList, Path serverPath) {
        Runnable refresh = () -> {
            //обновление листа для сервера
            serverListView.getItems().clear();
            network.getPathHolder().getServerPathMap().clear();
            network.getPathHolder().setServerPath(serverPath);
            serverFileList.stream().map(file -> file.toPath()).forEach((path) -> {
                System.out.println("Добавление в мапу /////////");
                System.out.println("Имя файла: " + path.getFileName());
                System.out.println("Путь:" + path.getParent());
                network.getPathHolder().getServerPathMap().put(path.getFileName().toString(),path);
            });
            System.out.println("Набор ключей: " + network.getPathHolder().getServerPathMap().keySet());
            serverListView.getItems().setAll(network.getPathHolder().getServerPathMap().keySet());
            if (network.getPathHolder().getServerPath()!=null)
                serverFolder.setText(network.getPathHolder().getServerPath().toString());
        };
        refreshPattern(refresh);
    }

    public void push() {
        ObservableList <String> os = localListView.getSelectionModel().getSelectedItems();
        network.filesHandler(os, Request.RequestType.SENDFILES);
    }

//    public void connect() {
//        Network.setController(this);
//        Network.getInstance().startNetwork("Suka", "Blyat");//заменить
//    }

    public void disconnest() {
        Platform.runLater(() -> Network.getInstance().shutdown());
        ClientMain.clientMain.setLogScene();
    }

    public void requestFile() {
        ObservableList<String> os = serverListView.getSelectionModel().getSelectedItems();
        network.sendFilesRequest(os, Request.RequestType.GETFILES);
    }

    public void stepBackServerPath(){
        //если запрашиваемый путь к папке на сервере эквивалентен корневому - запрос не выполняется.
        if(!network.getPathHolder().getServerPath().equals(PathHolder.baseServerPath)){
            Path path = network.getPathHolder().getServerPath().getParent();
            network.requestFilesListFromServer(path);
        }
    }

    public void stepBackClientPath(){
        Path path = Paths.get(PathHolder.baseLocalPath.toString(), network.getPathHolder().getClientPath().toString()).getParent();
        if (network.getPathHolder().getClientPath().compareTo(PathHolder.baseLocalPath) < 0) {
            return;
        }
        network.getPathHolder().setClientPath(PathHolder.baseLocalPath.relativize(path));
        System.out.println("Путь к папке клиента: " + network.getPathHolder().getClientPath());
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
                network.filesHandler(observableList, Request.RequestType.DELETEFILES);
                refreshLocalFilesList();
            }

            observableList = serverListView.getSelectionModel().getSelectedItems();
            if (!observableList.isEmpty()) {
                System.out.println("Нажата кнопка удаления файлов на сервере " + observableList);
                network.sendFilesRequest(observableList, Request.RequestType.DELETEFILES);
                network.requestFilesListFromServer(network.getPathHolder().getServerPath());
            }
    }

    public void closeApp() {
        Platform.runLater(() -> Network.getInstance().shutdown());
        Platform.exit();
    }
}
