package server;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final ConfigReader reader = ConfigReader.getInstance();
    private static final Logger log = LoggerFactory.getLogger(ChatServer.class);
    private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(reader.getPort())) {
            log.info("Server started on port {}", reader.getPort());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("New connection from {}", clientSocket.getRemoteSocketAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                threadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            log.error("Server connection error", e);
        }
    }

    static synchronized void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
        log.info("User '{}' connected. Total users: {}", username, clients.size());
        broadcastUserCount();
    }

    static synchronized void removeClient(String username) {
        clients.remove(username);
        log.info("User '{}' disconnected. Total users: {}", username, clients.size());
        broadcastUserCount();
    }

    static synchronized void broadcastMessage(String jsonMessage) {
        log.debug("Broadcasting message: {}", jsonMessage);
        for (ClientHandler client : clients.values()) {
            client.sendMessage(jsonMessage);
        }
    }

    static synchronized void broadcastUserCount() {
        int userCount = clients.size();
        log.info("Broadcasting user count: {}", userCount);
    }
}
