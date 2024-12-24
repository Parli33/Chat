package client;

import exceptions.ConnectException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat extends Application {
    private static final ConfigReader reader = ConfigReader.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Chat.class);

    private static Stage primaryStage;
    @Getter
    private static Socket clientSocket;
    @Getter
    private static BufferedReader serverReader;
    @Getter
    private static PrintWriter serverWriter;
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    public void start(Stage stage) {
        try {
            connectToServer();
        } catch (ConnectException e) {
            log.error("Failed to connect to server at {}:{}. Exception: {}", reader.getHost(), reader.getPort(), e.getMessage(), e);
            return;
        }

        primaryStage = stage;
        showLoginWindow();
    }

    public static void showLoginWindow() {
        loadScene("Login.fxml");
    }

    public static void showRegistrationWindow() {
        loadScene("Registration.fxml");
    }

    public static void showChatWindow() {
        loadScene("Chat.fxml");
    }

    private static void loadScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(Chat.class.getResource(fxmlFile));
            primaryStage.setScene(new Scene(loader.load()));
            primaryStage.getIcons().add(new Image("logo.png"));
            primaryStage.setTitle("Втелеграмме");
            primaryStage.show();
            log.info("Successfully loaded scene: {}", fxmlFile);
        } catch (Exception e) {
            log.error("Failed to load scene: {}", fxmlFile, e);
        }
    }

    public static void connectToServer() throws ConnectException {
        try {
            clientSocket = new Socket(reader.getHost(), reader.getPort());
            serverReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            serverWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            log.info("Successfully connected to server at {}:{}", reader.getHost(), reader.getPort());
        } catch (Exception e) {
            log.error("Error while connecting to server at {}:{}", reader.getHost(), reader.getPort(), e);
            throw new ConnectException("Failed to connect to server.");
        }
    }

    public static void main(String[] args) {
        launch();
        threadPool.shutdown();
        log.info("Application shutdown.");
    }

}