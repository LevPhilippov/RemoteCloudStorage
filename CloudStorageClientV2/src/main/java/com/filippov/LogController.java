package com.filippov;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LogController implements Initializable, MessageService {

    public LogController() {
        this.logController=this;
        Network.messageService = this;
    }

    public static LogController logController;

    @FXML
    private HBox cloudBox;

    @FXML
    private TextArea serviceMessageArea;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;


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
    public void initialize(URL location, ResourceBundle resources) {
        ImageView cloud = new ImageView(new Image("icons/MyCloud64.png"));
        cloudBox.getChildren().add(cloud);
    }

    @Override
    public void setServiseMessage(String message) {
        serviceMessageArea.appendText(message + "\n");
    }
}
