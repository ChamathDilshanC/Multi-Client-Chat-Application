package lk.ijse.Controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Optional;

import java.io.*;
import java.net.Socket;

public class ClientController {
    @FXML private TextField textField;
    @FXML private Button sendBtn;
    @FXML private Button addImageBtn;
    @FXML private Button disconnectbtn;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messageContainer;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Stage primaryStage;
    private boolean isConnected = false;
    private String clientName;

    public void initialize() {
        promptForName();
    }

    private void promptForName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Name");
        dialog.setHeaderText("Welcome to Chat Application");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            clientName = result.get().trim();
            connectToServer();
        } else {
            Platform.exit();
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 3000);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            dataOutputStream.writeUTF(clientName);

            isConnected = true;
            appendMessage("Connected to server as " + clientName, null);
            startMessageReceiving();
        } catch (IOException e) {
            appendMessage("Failed to connect to server.", null);
        }
    }

    private void startMessageReceiving() {
        new Thread(() -> {
            while (isConnected) {
                try {
                    int messageType = dataInputStream.readInt();
                    switch (messageType) {
                        case 0:
                            String message = dataInputStream.readUTF();
                            Platform.runLater(() -> appendMessage(message, null));
                            break;

                        case 1:
                            String fileName = dataInputStream.readUTF();
                            String senderName = dataInputStream.readUTF();
                            int fileSize = dataInputStream.readInt();
                            byte[] fileData = new byte[fileSize];
                            dataInputStream.readFully(fileData);

                            if (fileName.toLowerCase().endsWith(".png") ||
                                    fileName.toLowerCase().endsWith(".jpg") ||
                                    fileName.toLowerCase().endsWith(".jpeg")) {
                                Platform.runLater(() -> {
                                    try {
                                        Image image = new Image(new ByteArrayInputStream(fileData));
                                        appendMessage(senderName + " sent image: " + fileName, image);
                                    } catch (Exception e) {
                                        appendMessage("Failed to display image from " + senderName + ": " + fileName, null);
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    appendMessage(senderName + " sent file: " + fileName, null);
                                    saveFile(fileName, fileData);
                                });
                            }
                            break;
                    }
                } catch (IOException e) {
                    if (isConnected) {
                        Platform.runLater(() -> appendMessage("Connection lost: " + e.getMessage(), null));
                        disconnect();
                    }
                    break;
                }
            }
        }).start();
    }

    private void saveFile(String fileName, byte[] fileData) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(fileName);
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
                appendMessage("File saved: " + fileName, null);
            } catch (IOException e) {
                appendMessage("Failed to save file: " + fileName, null);
            }
        }
    }

    @FXML
    void sendBtnOnAction(ActionEvent event) {
        sendMessage();
    }

    @FXML
    void textFieldOnAction(ActionEvent event) {
        sendMessage();
    }

    @FXML
    void addImageBtnOnAction(ActionEvent event) {
        if (!isConnected) {
            appendMessage("Not connected to server.", null);
            return;
        }

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

                dataOutputStream.writeInt(1);
                dataOutputStream.writeUTF(selectedFile.getName());
                dataOutputStream.writeUTF(clientName);
                dataOutputStream.writeInt(fileData.length);
                dataOutputStream.write(fileData);
                dataOutputStream.flush();

                if (selectedFile.getName().toLowerCase().matches(".*\\.(png|jpg|jpeg)$")) {
                    Image image = new Image(new FileInputStream(selectedFile));
                    appendMessage("You sent image: " + selectedFile.getName(), image);
                } else {
                    appendMessage("You sent file: " + selectedFile.getName(), null);
                }
            } catch (IOException e) {
                appendMessage("Failed to send file: " + e.getMessage(), null);
            }
        }
    }

    private void sendMessage() {
        if (!isConnected) {
            appendMessage("Not connected to server.", null);
            return;
        }

        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            try {
                dataOutputStream.writeInt(0);
                dataOutputStream.writeUTF(clientName + ": " + message);
                dataOutputStream.flush();
                appendMessage("You: " + message, null);
                textField.clear();
            } catch (IOException e) {
                appendMessage("Failed to send message: " + e.getMessage(), null);
                disconnect();
            }
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

    private void disconnect() {
        isConnected = false;
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void disconnectBtnOnAction(ActionEvent event) {
        disconnect();
        Platform.exit();
    }
}