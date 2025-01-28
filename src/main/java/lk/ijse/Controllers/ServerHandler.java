package lk.ijse.Controllers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class ServerHandler {
    private static ServerHandler instance;
    private final List<ClientHandler> clients;
    private final ServerSocket serverSocket;
    private MessageCallback serverCallback;

    private ServerHandler() throws IOException {
        this.serverSocket = new ServerSocket(3000);
        this.clients = new CopyOnWriteArrayList<>();
    }

    public static ServerHandler getInstance() throws IOException {
        if (instance == null) {
            instance = new ServerHandler();
        }
        return instance;
    }

    public void setServerCallback(MessageCallback callback) {
        this.serverCallback = callback;
    }

    public void startAcceptingClients() {
        new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    DataInputStream tempDis = new DataInputStream(clientSocket.getInputStream());
                    String clientName = tempDis.readUTF();

                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientName);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();

                    if (serverCallback != null) {
                        serverCallback.onMessage(clientName + " connected. Total clients: " + clients.size(), null);
                    }

                    // Notify all clients about new user
                    broadcast(clientName + " joined the chat!", null);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void broadcastFile(String fileName, String senderName, byte[] fileData, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendFile(fileName, senderName, fileData);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        if (serverCallback != null) {
            serverCallback.onMessage(client.getClientName() + " disconnected. Total clients: " + clients.size(), null);
        }
        broadcast(client.getClientName() + " left the chat!", null);
    }

    public class ClientHandler implements Runnable {
        private final Socket socket;
        private final DataInputStream dataInputStream;
        private final DataOutputStream dataOutputStream;
        private final String clientName;

        public ClientHandler(Socket socket, String clientName) throws IOException {
            this.socket = socket;
            this.clientName = clientName;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        }

        public String getClientName() {
            return clientName;
        }

        @Override
        public void run() {
            try {
                while (socket.isConnected()) {
                    int messageType = dataInputStream.readInt();
                    switch (messageType) {
                        case 0:
                            String message = dataInputStream.readUTF();
                            if (serverCallback != null) {
                                serverCallback.onMessage(message, null);
                            }
                            broadcast(message, this);
                            break;

                        case 1:
                            String fileName = dataInputStream.readUTF();
                            String senderName = dataInputStream.readUTF();
                            int fileSize = dataInputStream.readInt();
                            byte[] fileData = new byte[fileSize];
                            dataInputStream.readFully(fileData);

                            boolean isImage = fileName.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif)$");
                            String messagePrefix = isImage ? " sent image: " : " sent file: ";

                            if (serverCallback != null) {
                                serverCallback.onMessage(senderName + messagePrefix + fileName, fileData);
                            }
                            broadcastFile(fileName, senderName, fileData, this);
                            break;
                    }
                }
            } catch (IOException e) {
                close();
            }
        }

        public void sendMessage(String message) {
            try {
                dataOutputStream.writeInt(0);
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
            } catch (IOException e) {
                close();
            }
        }

        public void sendFile(String fileName, String senderName, byte[] fileData) {
            try {
                dataOutputStream.writeInt(1);
                dataOutputStream.writeUTF(fileName);
                dataOutputStream.writeUTF(senderName);
                dataOutputStream.writeInt(fileData.length);
                dataOutputStream.write(fileData);
                dataOutputStream.flush();
            } catch (IOException e) {
                close();
            }
        }

        private void close() {
            removeClient(this);
            try {
                if (dataInputStream != null) dataInputStream.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}