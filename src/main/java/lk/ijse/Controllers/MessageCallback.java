package lk.ijse.Controllers;

public interface MessageCallback {
    void onMessage(String message, byte[] fileData);
}