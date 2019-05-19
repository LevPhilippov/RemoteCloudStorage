package com.filippov;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private Logger LOGGER;
    Network network;

    @FXML
    private VBox topBox;
    @FXML
    private TextArea serviceMessageArea;
    @FXML
    private Button pushButton, pullButton, deleteButton, closeAppButton, disconnectButton, backServerButton, backClientButton, propertyButton, refreshButton;
    @FXML
    private ListView serverListView, localListView;
    @FXML
    private TextField serverFolder, clientFolder, pushProgressField, pullProgressField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //binding fields
        controller = this;
        Network.messageService = this;
        LOGGER = LogManager.getLogger(this.getClass().getCanonicalName());
        network = Network.getInstance();

        //graphic setting
        CreateControllerGUI.setListenersOnListView(localListView,serverListView);
        CreateControllerGUI.bindIcons(topBox, pushButton, pullButton, deleteButton,disconnectButton, closeAppButton, backServerButton, backClientButton, propertyButton, refreshButton);

        //refresh lists
        refreshLocalFilesList();
        network.requestFilesListFromServer(null);
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
                LOGGER.trace("Добавление в мапу:\nИмя файла: {}\nПуть: {}",path.getFileName(),path.getParent() );
                network.getPathHolder().getServerPathMap().put(path.getFileName().toString(),path);
            });
            LOGGER.trace("Набор ключей: {}", network.getPathHolder().getServerPathMap().keySet());

            serverListView.getItems().setAll(network.getPathHolder().getServerPathMap().keySet());
            if (network.getPathHolder().getServerPath()!=null)
                serverFolder.setText(network.getPathHolder().getServerPath().toString());
        };
        refreshPattern(refresh);
    }

    public void push() {
        if (localListView.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
        ObservableList <String> os = localListView.getSelectionModel().getSelectedItems();
        network.filesHandler(os, Request.RequestType.SENDFILES);
    }

    public void disconnest() {
        Platform.runLater(() -> Network.getInstance().shutdown());
        ClientMain.clientMain.setLogScene();
    }

    public void pull() {
        if (serverListView.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
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
        LOGGER.trace("Путь к папке клиента: {}", network.getPathHolder().getClientPath());
        refreshLocalFilesList();
    }


    public static void refreshPattern(Runnable refresh) {
        if (Platform.isFxApplicationThread()) {
            refresh.run();
        } else {
            Platform.runLater(refresh);
        }
    }

    public void deleteButton() {
            ObservableList observableList = localListView.getSelectionModel().getSelectedItems();
            if(!observableList.isEmpty()) {
                LOGGER.trace("Нажата кнопка удаления локальных файлов: {} ", observableList);
                network.filesHandler(observableList, Request.RequestType.DELETEFILES);
                refreshLocalFilesList();
            }

            observableList = serverListView.getSelectionModel().getSelectedItems();
            if (!observableList.isEmpty()) {
                LOGGER.trace("Нажата кнопка удаления файлов на сервере: {} ", observableList);
                network.sendFilesRequest(observableList, Request.RequestType.DELETEFILES);
                network.requestFilesListFromServer(network.getPathHolder().getServerPath());
            }
    }

    public void closeApp() {
        Platform.runLater(() -> {
            if (network.networkIsActive())
                Network.getInstance().shutdown();
        });
        Platform.exit();
    }

    @Override
    public void setSingleServiseMessage(String message) {
        serviceMessageArea.appendText(message + "\n");
    }

    public void getFileProperty(ActionEvent actionEvent) {
        String key = (String) localListView.getSelectionModel().getSelectedItem();
        if(key != null) {
            LOGGER.trace("Запрос свойств файла клиента: {} ", key);
            Path path = network.getPathHolder().getClientPathMap().get(key);
            CreateControllerGUI.showFileProperty(new FileProperties(path.getFileName().toString(), path.getParent().toString(), path));
        }

        key = (String) serverListView.getSelectionModel().getSelectedItem();
        if (key != null) {
            LOGGER.trace("Запрос свойств файла сервера: {}", key);
            network.sendPropertyRequest(key);
        }
    }

    public void setPushProgress(String fileName, Long progress){
        Runnable runnable = () -> {
            String text = String.format("Pushing: %s Progress: %d %%", fileName, progress);
            pushProgressField.setText(text);
        };
        refreshPattern(runnable);
    }
    public void setPullProgress(String fileName, Long progress){
        Runnable runnable = () -> {
            String text = String.format("Pulling: %s Progress: %d %%", fileName, progress);
            pullProgressField.setText(text);
        };
        refreshPattern(runnable);
    }

    public void refreshLists(ActionEvent actionEvent) {
        refreshLocalFilesList();
        network.requestFilesListFromServer(network.getPathHolder().getServerPath());
    }
}
