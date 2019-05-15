package com.filippov;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LogController {

    public static LogController logController;

    public LogController() {
        this.logController=this;
    }
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

    public void setServiseText(String message) {
        serviceMessageArea.appendText(message + "\n");
    }
}
