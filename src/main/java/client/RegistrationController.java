package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;

    private final Gson gson = new Gson();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.equals(confirmPassword)) {
            JsonObject json = new JsonObject();
            json.addProperty("action", "register");
            json.addProperty("username", username);
            json.addProperty("password", password);

            Chat.getServerWriter().println(gson.toJson(json));

            try {
                String response = Chat.getServerReader().readLine();
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
                if ("success".equals(jsonResponse.get("status").getAsString())) {
                    Chat.showLoginWindow();
                } else {
                    showAlert(jsonResponse.get("message").getAsString(), Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Не удалось подключиться к серверу.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Пароли не совпадают.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, javafx.scene.control.ButtonType.OK);
        alert.showAndWait();
    }

    @FXML
    public void backToLogin(ActionEvent actionEvent) {
        Chat.showLoginWindow();
    }
}
