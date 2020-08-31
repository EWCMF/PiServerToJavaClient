import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartJavaFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Client");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> System.exit(0));

        Client client = loader.getController();
        client.read();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
