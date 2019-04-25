package com.filippov;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import lombok.Getter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Getter
public class Controller implements Initializable {

    @FXML
    private ListView serverListView;

    @FXML
    private ListView localListView;

    @FXML
    private Text serverStatusField;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshLocalFileList();
    }


    public void refreshLocalFileList() {
        String path = "CloudStorageClientV2/Storage/";
        if (Platform.isFxApplicationThread()) {
            localListView.getItems().clear();
            Factory.giveFileList(path).stream().forEach(localListView.getItems()::add);
        } else {
            Platform.runLater(() -> {
                localListView.getItems().clear();
                Factory.giveFileList(path).stream().forEach(localListView.getItems()::add);
            });
        }

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
//                for (String s : serverFileList) {
//                    serverListView.getItems().add(s);
//                }
        } else {
            Platform.runLater(() -> {
                    serverListView.getItems().clear();
                    serverFileList.stream().forEach(serverListView.getItems()::add);
            });
        }

//        serverListView.setManaged(true);
//        serverListView.getItems().clear();
//        for (String s : serverFileList) {
//            serverListView.getItems().add(s);
//        }
    }

    public void requestFile() {
        ObservableList<String> os = serverListView.getSelectionModel().getSelectedItems();
        Network.getInstance().requestFile(os, "CloudStorageServer/Storage", "CloudStorageClientV2/Storage");
    }
}
