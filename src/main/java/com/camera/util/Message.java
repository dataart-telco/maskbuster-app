package com.camera.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Message {
    private static final ConcurrentMap<String, Boolean> openedDialogs = new ConcurrentHashMap<>();


    public static void error(String message) {
        Platform.runLater(() -> {
            // don't open a lot of popups with the same message
            if (openedDialogs.containsKey(message)) {
                return;
            }
            openedDialogs.put(message, true);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.showAndWait();
            openedDialogs.remove(message);
        });
    }
}
