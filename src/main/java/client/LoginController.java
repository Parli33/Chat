package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final Gson gson = new Gson();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        JsonObject json = new JsonObject();
        json.addProperty("action", "login");
        json.addProperty("username", username);
        json.addProperty("password", password);

        Chat.getServerWriter().println(gson.toJson(json));

        try {
            String response = Chat.getServerReader().readLine();
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            if ("success".equals(jsonResponse.get("status").getAsString())) {
                Chat.showChatWindow();
            } else {
                showAlert(jsonResponse.get("message").getAsString());
            }
        } catch (Exception e) {
            showAlert("Не удалось подключиться к серверу.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, javafx.scene.control.ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void openRegistration(ActionEvent actionEvent) {
        Chat.showRegistrationWindow();
    }

    public void handleEnterKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }
}
