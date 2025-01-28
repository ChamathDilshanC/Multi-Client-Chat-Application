package lk.ijse.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class ServerController implements MessageCallback {
    @FXML private TextField textField;
    @FXML private Button sendBtn;
    @FXML private Button addImageBtn;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messageContainer;

    private Stage primaryStage;
    private ServerHandler serverHandler;

    public void initialize() {
        try {
            serverHandler = ServerHandler.getInstance();
            serverHandler.setServerCallback(this);
            serverHandler.startAcceptingClients();
            appendMessage("Server Started. Waiting for clients...", null);
        } catch (IOException e) {
            e.printStackTrace();
            appendMessage("Failed to start server.", null);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void onMessage(String message, byte[] fileData) {
        Platform.runLater(() -> {
            if (fileData != null) {
                try {
                    Image image = new Image(new ByteArrayInputStream(fileData));
                    appendMessage(message, image);
                } catch (Exception e) {
                    appendMessage(message + " (File received)", null);
                }
            } else {
                appendMessage(message, null);
            }
        });
    }

    @FXML
    public void textFieldOnAction(ActionEvent event) {
        sendMessage();
    }

    @FXML
    public void sendBtnOnAction(ActionEvent event) {
        sendMessage();
    }

    @FXML
    public void addImageBtnOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                byte[] fileData = new byte[(int) selectedFile.length()];
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    fis.read(fileData);
                }
                serverHandler.broadcastFile(selectedFile.getName(), "Server", fileData, null);

                if (selectedFile.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg)$")) {
                    Image image = new Image(new FileInputStream(selectedFile));
                    appendMessage("Server sent image: " + selectedFile.getName(), image);
                } else {
                    appendMessage("Server sent file: " + selectedFile.getName(), null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                appendMessage("Failed to load file.", null);
            }
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            serverHandler.broadcast("Server: " + message, null);
            appendMessage("Server: " + message, null);
            textField.clear();
        }
    }

    private void appendMessage(String message, Image image) {
        Text text = new Text(message + "\n");
        messageContainer.getChildren().add(text);

        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(150);
            imageView.setPreserveRatio(true);
            messageContainer.getChildren().add(imageView);
        }

        scrollPane.setVvalue(1.0);
    }
}