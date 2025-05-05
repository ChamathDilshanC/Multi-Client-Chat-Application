# Multi-Client Chat Application

![GitHub](https://img.shields.io/github/license/ChamathDilshanC/Multi-Client-Chat-Application)
![Java](https://img.shields.io/badge/Java-Socket%20Programming-orange)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

A real-time chat application implemented using Java Socket Programming, enabling multiple clients to connect to a centralized server and communicate with each other.

## 🌟 Features

- **Multi-client support**: Connect multiple users simultaneously
- **Real-time messaging**: Instant message delivery between clients
- **Threaded architecture**: Each client connection is handled in a separate thread
- **User notifications**: Alerts when users join or leave the chat
- **Simple and intuitive interface**: Easy-to-use command-line interface
- **Robust error handling**: Graceful management of unexpected disconnections

## 📋 Prerequisites

- Java JDK 8 or higher
- Basic understanding of networking concepts

## 🚀 Getting Started

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/ChamathDilshanC/Multi-Client-Chat-Application.git
   cd Multi-Client-Chat-Application
   ```

2. Compile the source code:
   ```bash
   javac *.java
   ```

### Running the Application

1. Start the server:
   ```bash
   java Server
   ```

2. Launch the client(s) in separate terminal windows:
   ```bash
   java Client
   ```

3. Follow the on-screen instructions to join the chat.

## 🏗️ Architecture

The application follows a client-server architecture:

```
                  ┌─────────┐
                  │  Server │
                  └────┬────┘
                       │
           ┌───────────┴───────────┐
           │           │           │
      ┌────▼─────┐ ┌────▼─────┐ ┌────▼─────┐
      │ Client 1 │ │ Client 2 │ │ Client 3 │
      └──────────┘ └──────────┘ └──────────┘
```

- **Server**: Manages client connections and routes messages between clients
- **Client**: Connects to the server and provides a user interface for sending/receiving messages

## 💻 Implementation Details

### Server Components

- `ServerSocket`: Listens for incoming client connections
- `ClientHandler`: Manages individual client connections (implemented as threads)
- Message broadcasting system to relay messages to all connected clients

### Client Components

- `Socket`: Establishes connection with the server
- Input handling for user messages
- Output handling for displaying received messages

## 🎮 Usage Guide

### Server Operations

Once the server is running, it will display connection information and wait for clients to connect.

### Client Operations

After starting a client:

1. Enter a username when prompted
2. Start typing messages to communicate with other connected users
3. Use the `/quit` command to exit the chat

## 🚧 Limitations and Future Improvements

- Currently only supports local network connections
- No message persistence (history is lost when the server restarts)
- No private messaging between specific users

Future enhancements planned:
- Add user authentication
- Implement private messaging
- Create a graphical user interface
- Add message persistence
- Enable file sharing between users

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Contact

Chamath Dilshan - [@ChamathDilshanC](https://github.com/ChamathDilshanC)

Project Link: [https://github.com/ChamathDilshanC/Multi-Client-Chat-Application](https://github.com/ChamathDilshanC/Multi-Client-Chat-Application)

## 🙏 Acknowledgements

- [Java Socket Programming Documentation](https://docs.oracle.com/javase/tutorial/networking/sockets/index.html)
- All contributors who have helped improve this project
