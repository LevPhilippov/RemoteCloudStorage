package com.filippov;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LogController implements Initializable, MessageService {

    public LogController() {
        this.logController=this;
        Network.messageService = this;
    }

    @FXML
    private Button log, createId;

    public static LogController logController;
    @FXML
    private HBox cloudBox;
    @FXML
    private TextArea serviceMessageArea;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ImageView cloud = new ImageView(new Image("icons/MyCloud64.png"));
        cloudBox.getChildren().add(cloud);
        log.setDisable(true);
        createId.setDisable(true);

        EventHandler<KeyEvent> keyEventEventHandler = event -> {
            if(loginField.getText().length()<2 || passwordField.getText().length()<4) {
                log.setDisable(true);
                createId.setDisable(true);
            } else {
                log.setDisable(false);
                createId.setDisable(false);
            }
        };

        loginField.setOnKeyPressed(keyEventEventHandler);
        passwordField.setOnKeyPressed(keyEventEventHandler);
    }

    public void tryNewId() {
        try {
            Network.getInstance().requestAuth(new AuthData(loginField.getText(), passwordField.getText(), true), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryAuth() {
        try {

            Network.getInstance().requestAuth(new AuthData(loginField.getText(), passwordField.getText(), false), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TextField getLoginField() {
        return loginField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    @Override
    public void setSingleServiseMessage(String message) {
        serviceMessageArea.appendText(message + "\n");
    }
}
