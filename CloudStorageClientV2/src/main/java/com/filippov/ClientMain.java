package com.filippov;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientMain extends Application {

    private static Parent root;
    private static Parent log;
    private static Stage primaryStage;
    public  static ClientMain clientMain;

    @Override
    public void start(Stage primaryStage) throws Exception{
        clientMain=this;
        this.primaryStage = primaryStage;
//        root = FXMLLoader.load(getClass().getResource("/log.fxml"));
//        primaryStage.setTitle("Cloud");
//        primaryStage.setScene(new Scene(root, 300, 200));
//        primaryStage.setResizable(false);
//        primaryStage.show();
        setMainScene();
    }

    public void setMainScene(){
        Runnable runnable = () -> {
            try {
                root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            primaryStage.setTitle("Cloud");
            primaryStage.setScene(new Scene(root, 800, 600));
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
