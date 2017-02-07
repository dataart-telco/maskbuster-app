package com.camera;

import java.io.IOException;

import com.camera.util.Settings;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
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
        launch(args);
    }
}
