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
import java.net.Socket;

public class Client1Controller {

    public Button disconnectbtn;
    @FXML private TextField textField;
    @FXML private Button sendBtn;
    @FXML private Button addImageBtn;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messageContainer;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Stage primaryStage;

    public void initialize() {
        connectToServer();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 3000);
            Platform.runLater(() -> appendMessage("Connected to server.", null));

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> appendMessage("Failed to connect to server.", null));
        }
    }

    private void receiveMessages() {
        try {
            while (true) {
                int messageType = dataInputStream.readInt();
                if (messageType == 1) { // Image
                    int imageSize = dataInputStream.readInt();
                    byte[] imageBuffer = new byte[imageSize];
                    dataInputStream.readFully(imageBuffer);

                    InputStream inputStream = new ByteArrayInputStream(imageBuffer);
                    Image image = new Image(inputStream);

                    Platform.runLater(() -> appendMessage("Image received:", image));
                } else { // Text message
                    String message = dataInputStream.readUTF();
                    Platform.runLater(() -> appendMessage("Server: " + message, null));
                }
            }
        } catch (IOException e) {
            Platform.runLater(() -> appendMessage("Disconnected from server.", null));
        } finally {
            closeConnection();
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.mp4")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                sendImage(image, selectedFile.getPath());
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                appendMessage("Failed to load image.", null);
            }
        }
    }

    private void sendMessage() {
        String message = textField.getText().trim();
        if (!message.isEmpty()) {
            try {
                dataOutputStream.writeInt(0); // 0 for text message
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                appendMessage("You: " + message, null);
                textField.clear();
            } catch (IOException e) {
                e.printStackTrace();
                appendMessage("Failed to send message.", null);
            }
        }
    }

    private void sendImage(Image image, String imagePath) {
        try {
            File file = new File(imagePath);
            byte[] imageData = new byte[(int) file.length()];
            FileInputStream fis = new FileInputStream(file);
            fis.read(imageData);
            fis.close();

            dataOutputStream.writeInt(1); // 1 for image message
            dataOutputStream.writeInt(imageData.length);
            dataOutputStream.write(imageData);
            dataOutputStream.flush();

            appendMessage("You: Image sent", image);
        } catch (IOException e) {
            e.printStackTrace();
            appendMessage("Failed to send image.", null);
        }
    }

    private void appendMessage(String message, Image image) {
        Text text = new Text(message + "\n");
        messageContainer.getChildren().add(text);

        if (image != null) {
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            messageContainer.getChildren().add(imageView);
        }

        // Scroll to the bottom
        scrollPane.setVvalue(1.0);
    }

    private void closeConnection() {
        try {
            if (dataInputStream != null) dataInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectBtnOnAction(ActionEvent actionEvent) {
        System.exit(0);
    }
}