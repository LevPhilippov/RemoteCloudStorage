package com.filippov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClientMain extends Application {
    private static final Logger LOGGER = LogManager.getLogger(ClientMain.class.getCanonicalName());
    private static Parent root;
    private Stage primaryStage;
    public static ClientMain clientMain;

    @Override
    public void start(Stage primaryStage) throws Exception{
        clientMain=this;
        this.primaryStage = primaryStage;

        setLogScene();
    }

    public void setMainScene(){
        Runnable runnable = () -> {
            try {
                root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
            } catch (IOException e) {
                LOGGER.error("Ошибка создания основной сцены!\n" + e.getMessage());
            }
            primaryStage.setTitle("MyCloud");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(event -> {
                if(Network.getInstance().networkIsActive()) {
                    Network.getInstance().shutdown();
                }
            });
            primaryStage.show();
        };
        refreshPattern(runnable);
    }

    public void setLogScene(){
        Runnable runnable = () -> {
            try {
                root = FXMLLoader.load(getClass().getResource("/log.fxml"));
            } catch (IOException e) {
                LOGGER.error("Ошибка создания лог-сцены!\n" + e.getMessage());
            }
            primaryStage.setTitle("MyCloud");
            primaryStage.setScene(new Scene(root, 400, 300));
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(event -> {
                if(Network.getInstance().networkIsActive()) {
                    Network.getInstance().shutdown();
                }
            });

            primaryStage.show();
        };
        refreshPattern(runnable);
    }

    private static void refreshPattern(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
