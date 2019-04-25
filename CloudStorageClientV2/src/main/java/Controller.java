import com.filippov.CloudWrappedObject;
import com.filippov.Factory;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
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
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fillLists(localListView);
    }


    private static void fillLists(ListView localListView) {
        String path = "CloudStorageClientV2/Storage/";
        List<String> pathList = Factory.giveFileList(path);
        ObservableList<String> observableList = FXCollections.observableList(pathList);
        localListView.getItems().clear();
        System.out.println(path.toString());
        localListView.setItems(observableList);
    }

    public void synchronize() {
        ObservableList <String> os = localListView.getSelectionModel().getSelectedItems();
        Network.getInstance().synchronize(os);
    }

    public void connect() {
        Network.setController(this);
        Network.getInstance().startNetwork();
    }

    public void disconnest() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().shutdown();
            }
        });
        Platform.exit();
    }

    public void refreshServerFileList(List<String> serverFileList) {
        serverListView.setManaged(true);
        serverListView.getItems().clear();
        for (String s : serverFileList) {
            serverListView.getItems().add(s);
        }
    }

    public void requestFile() {
        ObservableList <String> os = serverListView.getSelectionModel().getSelectedItems();
        Network.getInstance().requestFile(os);
    }
}
