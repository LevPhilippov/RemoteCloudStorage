package com.filippov;

import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateControllerGUI {

    private static Button pushButton, pullButton, deleteButton,disconnectButton, closeAppButton, backServerButton, backClientButton, propertyButton;
    /**
     * Порядок: pushButton, pullButton, deleteButton,disconnectButton, closeAppButton, backServerButton, backClientButton
     * */
    public static void bindIcons(Pane pane, Button...buttons) {
        pushButton = buttons[0];
        pullButton = buttons[1];
        deleteButton= buttons[2];
        disconnectButton = buttons[3];
        closeAppButton = buttons[4];
        backServerButton = buttons[5];
        backClientButton = buttons[6];
        propertyButton = buttons[7];


        List<ImageView> imageViews = new ArrayList<>();
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
        //property
        imageViews.add(new ImageView(new Image("icons/property64.png")));
        for (ImageView imageView : imageViews) {
            imageView.setFitHeight(30);
            imageView.setFitWidth(30);
        }
        pushButton.setGraphic(imageViews.get(0));
        pullButton.setGraphic(imageViews.get(1));
        deleteButton.setGraphic(imageViews.get(2));
        disconnectButton.setGraphic(imageViews.get(3));
        closeAppButton.setGraphic(imageViews.get(4));
        propertyButton.setGraphic(imageViews.get(5));

        ImageView backImage = new ImageView(new Image("icons/back16.png"));
        ImageView backImage2 = new ImageView(new Image("icons/back16.png"));
        backServerButton.setGraphic(backImage2);
        backClientButton.setGraphic(backImage);
        ImageView logo = new ImageView(new Image("icons/MyCloud64.png"));
        pane.getChildren().add(logo);
    }

    public static void setListenersOnListView(ListView localListView, ListView serverListView) {
        localListView.setManaged(true);
        serverListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        serverListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        localListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==1) {
                    serverListView.getSelectionModel().clearSelection();
                    backServerButton.setDisable(true);
                    pullButton.setDisable(true);
                    pushButton.setDisable(false);
                    backClientButton.setDisable(false);
                }
                if(event.getClickCount()==2) {
                    if (localListView.getSelectionModel().getSelectedItems().isEmpty()){
                        return;
                    }
                    Path path = Paths.get(PathHolder.baseLocalPath.toString(), Network.getInstance().getPathHolder().getClientPath().toString(), (String)localListView.getSelectionModel().getSelectedItem());
//                    System.out.println(path.toString());
                    if(Files.isDirectory(path)) {
//                        System.out.println("-----------------------------");
//                        System.out.println("Новый путь к директории клиента: " + path.toString());
//                        System.out.println("-----------------------------");
                        Network.getInstance().getPathHolder().setClientPath(PathHolder.baseLocalPath.relativize(path));
                        Controller.controller.refreshLocalFilesList();
                        return;
                    }
//                    System.out.println("Выбранный файл не является директорией!");
                }
            }
        });

        serverListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount()==1) {
                    localListView.getSelectionModel().clearSelection();
                    backServerButton.setDisable(false);
                    pullButton.setDisable(false);
                    pushButton.setDisable(true);
                    backClientButton.setDisable(true);
                }
                if(event.getClickCount()==2) {
                    if (serverListView.getSelectionModel().getSelectedItems().isEmpty()){
                        return;
                    }
                    Path path = Paths.get(Network.getInstance().getPathHolder().getServerPath().toString(),(String)serverListView.getSelectionModel().getSelectedItem());
//                    System.out.println("Запрашиваю список файлов сервера в каталоге: " + path.toString());
                    Network.getInstance().requestFilesListFromServer(path);
                }
            }
        });
    }

    public static void showFileProperty(FileProperties fileProperties) {
        // Создаем Alert, указываем текст и кнопки, которые на нем должны быть
        String contentText = String.format("File name: %s\nPath: %s\nSize: %s\nLast Modificated at: %s\n",
                fileProperties.getFileName(),
                fileProperties.getFilePath(),
                fileProperties.getSize(),
                fileProperties.getLastModificationTime());

        Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText, ButtonType.OK);
        // showAndWait() показывает Alert и блокирует остальное приложение пока мы не закроем Alert
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get().getText().equals("OK")) {
        }
    }
}
