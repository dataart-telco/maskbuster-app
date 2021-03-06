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

    @Override
    public void start(Stage stage) throws IOException {
        stage.setOnCloseRequest(event -> SettingsController.setEnableVideoStreaming(false));
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
        Settings.load();
        configLogger();
        launch(args);
    }

    private static void configLogger() {
        PropertyConfigurator.configure(Settings.getLogPropertiesPath());
    }
}
