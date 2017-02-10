package com.camera;

import java.io.IOException;

import com.camera.controller.SettingsController;
import com.camera.util.Settings;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.*;

public class Main extends Application {

    private static Logger logger = Logger.getRootLogger();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setOnCloseRequest(event -> SettingsController.setEnableVideoStreaming(false));
        Settings.load();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app2.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1180, 590);
        stage.setTitle("MaskBuster");
        stage.setScene(scene);
        stage.setMinHeight(590);
        stage.setMinWidth(1180);
        stage.setMaximized(true);
        stage.show();
    }
    public static void main(String[] args) {
        configLogger();
        launch(args);
    }

    private static void configLogger() {
        PatternLayout patternLayout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
        RollingFileAppender appender = null;
        try {
            appender = new RollingFileAppender(patternLayout, Settings.getLogFilePath());
            appender.setMaxFileSize("30MB");
            appender.setMaxBackupIndex(10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
    }
}
