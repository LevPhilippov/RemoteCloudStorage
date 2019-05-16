package com.filippov;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable, MessageService  {

    public static Controller controller;

    Network network;

    @FXML
    private VBox topBox;
    @FXML
    private TextArea serviceMessageArea;
    @FXML
    private Button pushButton;
    @FXML
    private Button pullButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button closeAppButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private Button backServerButton;
    @FXML
    private Button backClientButton;

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
        Network.messageService = this;
        //network binding
        network = Network.getInstance();
//        network.setController(this);
        //listview setting
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setListenersOnListView();
        //icons
        bindIcons();
        //refresh lists
        refreshLocalFilesList();
        network.requestFilesListFromServer(null);
    }

    private void bindIcons() {
        List <ImageView> imageViews = new ArrayList<>();
        //push
        imageViews.add(new ImageView(new Image("icons/doubleArrowLeft48.png")));
        //pull
        imageViews.add(new ImageView(new Image("icons/doubleArrowRight48.png")));
        //delete
        imageViews.add(new ImageView(new Image("icons/recycle100.png")));
        //disconnect
        imageViews.add(new ImageView(new Image("icons/disconnect64.png")));
        //shutdown
        imageViews.add(new ImageView(new Image("icons/shutdown48.png")));
        for (ImageView imageView : imageViews) {
            imageView.setFitHeight(30);
            imageView.setFitWidth(30);
        }
        pushButton.setGraphic(imageViews.get(0));
        pullButton.setGraphic(imageViews.get(1));
        deleteButton.setGraphic(imageViews.get(2));
        disconnectButton.setGraphic(imageViews.get(3));
        closeAppButton.setGraphic(imageViews.get(4));

        ImageView backImage = new ImageView(new Image("icons/back16.png"));
        ImageView backImage2 = new ImageView(new Image("icons/back16.png"));
        backServerButton.setGraphic(backImage2);
        backClientButton.setGraphic(backImage);
        ImageView logo = new ImageView(new Image("icons/MyCloud64.png"));
        topBox.getChildren().add(logo);

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

    public void deleteButton() {
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

    @Override
    public void setServiseMessage(String message) {
        serviceMessageArea.appendText(message + "\n");
    }
}
