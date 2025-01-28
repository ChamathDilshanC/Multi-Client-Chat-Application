package lk.ijse;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        showWindow(primaryStage, "/view/Server.fxml", "Server");

        Stage clientStage = new Stage();
        showWindow(clientStage, "/view/Client.fxml", "Client");

        Stage client1Stage = new Stage();
        showWindow(client1Stage, "/view/Client.fxml", "Client");

        // Position windows
        primaryStage.setX(100);
        clientStage.setX(primaryStage.getX() + primaryStage.getWidth() + 10);
        client1Stage.setX(clientStage.getX() + clientStage.getWidth() + 10);
    }

    private void showWindow(Stage stage, String fxmlPath, String title) throws Exception {
        Parent rootNode = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(rootNode);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();

        // Prevent window maximization
        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                stage.setMaximized(false);
            }
        });
    }
}