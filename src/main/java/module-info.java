module org.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires static lombok;
    requires com.google.gson;
    requires java.sql;
    requires javafx.graphics;
    requires spring.security.crypto;

    exports server;
    opens server to javafx.fxml;
    exports client;
    opens client to javafx.fxml;
    exports utils;
    opens utils to javafx.fxml;
}