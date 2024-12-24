package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter writer;
    private String username;
    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);
    private final Gson gson = new Gson();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            this.writer = writer;
            String received;

            while ((received = reader.readLine()) != null) {
                log.info("Message received: {}", received);

                try {
                    JsonObject json = gson.fromJson(received, JsonObject.class);
                    String action = json.get("action").getAsString();

                    switch (action) {
                        case "register":
                            handleRegistration(json);
                            break;
                        case "login":
                            handleLogin(json);
                            break;
                        case "message":
                            handleChatMessage(json);
                            break;
                        default:
                            sendError("Unknown action");
                    }
                } catch (Exception e) {
                    log.error("Invalid JSON format: {}", received, e);
                    sendError("Invalid JSON format");
                }
            }
        } catch (IOException e) {
            log.error("Client handler error", e);
        } finally {
            ChatServer.removeClient(username);
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.error("Error closing client socket", e);
            }
        }
    }

    private void handleRegistration(JsonObject json) {
        try {
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            DatabaseController.registerUser(username, password);
            sendSuccess("Registration successful");
        } catch (SQLException e) {
            sendError("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin(JsonObject json) {
        try {
            String username = json.get("username").getAsString();
            String password = json.get("password").getAsString();
            if (DatabaseController.authenticateUser(username, password)) {
                this.username = username;
                ChatServer.addClient(username, this);
                sendSuccess("Login successful");
            } else {
                sendError("Invalid username or password");
            }
        } catch (Exception e) {
            sendError("Login failed: " + e.getMessage());
        }
    }

    private void handleChatMessage(JsonObject json) {
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("username", username);
        responseJson.addProperty("message", json.get("message").getAsString());
        ChatServer.broadcastMessage(responseJson.toString());
    }

    private void sendError(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        writer.println(gson.toJson(response));
    }

    private void sendSuccess(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("message", message);
        writer.println(gson.toJson(response));
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
}
