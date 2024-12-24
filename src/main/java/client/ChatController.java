package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.BufferedReader;
import java.io.IOException;

public class ChatController {
    @FXML
    private ListView<String> userList;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;

    private final Gson gson = new Gson();

    public void initialize() {
        new Thread(() -> {
            try {
                BufferedReader reader = Chat.getServerReader();
                String received;

                while ((received = reader.readLine()) != null) {
                    try {

                        JsonObject json = gson.fromJson(received, JsonObject.class);
                        String username = json.get("username").getAsString();
                        String message = json.get("message").getAsString();

                        chatArea.appendText(username + ": " + message + "\n");
                    } catch (JsonSyntaxException e) {
                        System.err.println("Invalid JSON received: " + received);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isBlank()) {
            JsonObject json = new JsonObject();
            json.addProperty("action", "message");
            json.addProperty("message", message);

            Chat.getServerWriter().println(gson.toJson(json));
            messageField.clear();
        }
    }

    @FXML
    public void handleEnterKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendMessage();
        }
    }
}
