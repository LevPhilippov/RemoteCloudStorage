import com.filippov.CloudWrappedObject;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView serverListView;

    @FXML
    private ListView localListView;

    @FXML
    private Text serverStatusField;

    Network network = new Network();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(network).start();
        localListView.setManaged(true);
        localListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fillLists(null, localListView);

    }


    private static void fillLists(Path path, ListView localListView) {
        path = Paths.get("CloudStorageClientV2/Storage");
        List<Path> pathList = new ArrayList<>();
        HashSet<Path> filesSet = new HashSet<>();
        ObservableList<Path> observableList = FXCollections.observableList(pathList);
        localListView.getItems().clear();
        System.out.println(path.toString());
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    filesSet.add(file.getFileName());
                    pathList.add(file.getFileName());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            localListView.setItems(observableList);
        }
    }

    public void synchronize() {
        ObservableList <Path> os = localListView.getSelectionModel().getSelectedItems();
        for (Path o : os) {

        }
    }

    public void echo() {
        CloudWrappedObject c = new CloudWrappedObject();
        network.getCf().channel().write(c);
    }

//    public void setServerStatus() {
//        this.serverStatusField.setText("Server is online");
//    }
}
