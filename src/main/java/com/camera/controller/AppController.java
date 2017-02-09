package com.camera.controller;

import com.camera.AlertCall;
import com.camera.fx.DigitalClock;
import com.camera.thread.CapturingThread;
import com.camera.util.AlarmColors;
import com.camera.util.DatetimeUtils;
import com.camera.util.GuiProxy;
import com.camera.util.Settings;
import com.github.sarxos.webcam.Webcam;
import com.jfoenix.controls.JFXPopup;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class AppController {
    private static final int CAPTURE_BLINK_LENGTH_MILLIS = 500;
    private final Font latoSemibold_43;
    private final Font latoRegular_16;
    private final Font latoRegular_14;
    private static final int ROTATION_ANGLE = 30;
    private static AtomicLong alarmDuration = new AtomicLong(3000);
    private static AtomicLong alarmHighlightDuration = new AtomicLong(Settings.alarmHighlightDuration());
    private final Logger logger = LoggerFactory.getLogger(AppController.class);
    private final Image settingsIcon;
    private final Image activeSettingsIcon;
    private final Image alertSettingIcon;
    private final Image cameraIcon;
    private final GuiProxy guiProxy;
    private final AlertCall alertCall;
    private final VBox logBox;
    private final VBox captureBox;
    private final AtomicBoolean alertingInProgress;
    private final Image logo;
    private final Image logoAlarm;
    private RotateTransition rotateTransition;

    @FXML
    private DigitalClock digitalClock;

    @FXML
    private GridPane rootPane;

    @FXML
    private ImageView settings;

    @FXML
    private JFXPopup settingsMenu;

    @FXML
    private VBox videoBox;

    @FXML
    private VBox rightSideMenu;

    @FXML
    private HBox clockHolder;

    @FXML
    private Label capturesLabel;

    @FXML
    private Label logsLabel;

    @FXML
    private ImageView dartLogo;

    @FXML
    private Label processingLabel;

    private Label alertLabel;

    private long captureIndex;

    private VideoController videoController;

    public AppController() {
        this.guiProxy = new GuiProxyImpl();
        this.alertCall = new AlertCall(guiProxy);
        this.logBox = new VBox();
        this.captureBox = new VBox();
        this.alertingInProgress = new AtomicBoolean(false);
        this.settingsIcon = new Image(getClass().getResourceAsStream("/icons/config.png"));
        this.activeSettingsIcon = new Image(getClass().getResourceAsStream("/icons/config-clicked.png"));
        this.alertSettingIcon = new Image(getClass().getResourceAsStream("/icons/settings-on_red.png"));
        this.cameraIcon = new Image(getClass().getResourceAsStream("/icons/camera.png"));
        this.latoSemibold_43 = Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Semibold.ttf"), 43D);
        this.latoRegular_16 = Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Regular.ttf"), 16D);
        this.latoRegular_14 = Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Regular.ttf"), 14D);
        this.logo = new Image(getClass().getResourceAsStream("/icons/dataart.png"));
        this.logoAlarm = new Image(getClass().getResourceAsStream("/icons/dataart-red.png"));
        this.alertLabel = new Label("SKI MASK DETECTED!");
        this.videoController = new VideoController();

        alertLabel.setTextFill(Paint.valueOf("white"));
        alertLabel.setFont(latoSemibold_43);
        alertLabel.setPadding(new Insets(0, 0, 13, 10));
        alertLabel.setVisible(false);
    }

    public static void setAlarmDuration(Long alarmDuration) {
        AppController.alarmDuration.set(alarmDuration);
    }

    public static void setAlarmHighlightDuration(Long alarmHighlightDuration) {
        AppController.alarmHighlightDuration.set(alarmHighlightDuration);
    }

    @FXML
    private void initialize() {
        guiProxy.writeMessage("Initializing the application");
        videoBox.getChildren().add(videoController);
        clockHolder.getChildren().add(alertLabel);

        displayCaptures();
        loadCaptures();

        rotateTransition = new RotateTransition(Duration.millis(500), settings);
        rotateTransition.setAutoReverse(true);

        initMenuListener();
        initPopup();
        processingLabel.setVisible(false);
        (new CapturingThread(guiProxy)).start();
    }

    @FXML
    void openSettingsMenu(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (!settingsMenu.isVisible()) {
                settingsMenu.show(JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.RIGHT, settings.getX(),
                        settings.getY() + 146);
                settingsMenu.toFront();
                Platform.runLater(() -> settings.toFront());
            } else {
                settingsMenu.close();
            }
        }
    }

    /**
     * Loads popup content and configure it
     */
    private void initPopup() {
        AnchorPane pane = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/menu.fxml"));
        SettingsController sc = new SettingsController(videoController);
        fxmlLoader.setController(sc);
        try {
            pane = fxmlLoader.load();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        settingsMenu.setContent(pane);
        settingsMenu.setSource(settings);
        settingsMenu.setStyle("-fx-border-radius: 10 10 0 0;");

        rootPane.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (settingsMenu.isVisible()) {
                settingsMenu.close();
            }
        });
    }

    /**
     * This method helps to ensure that settings icon will change upon popup
     * menu closing
     */
    private void initMenuListener() {
        settingsMenu.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                rotateTransition.setByAngle(ROTATION_ANGLE);
                rotateTransition.play();
                settings.setImage(activeSettingsIcon);
            } else {
                rotateTransition.setByAngle(-ROTATION_ANGLE);
                rotateTransition.play();
                settings.setImage(alertingInProgress.get() ? alertSettingIcon : settingsIcon);
             //   settingsMenu.close();
            }
        });
    }

    /**
     * Displays captures side bar when "Captures" label is clicked.
     */
    public void displayCaptures() {
        rightSideMenu.getChildren().clear();
        rightSideMenu.getChildren().add(captureBox);
        capturesLabel.setStyle("-fx-border-width:  0 0 2 0; -fx-border-color: white; -fx-font-weight: 900");
        logsLabel.setStyle("-fx-border-width:  0 0 0 0; -fx-font-weight: normal; -fx-opacity: 0.75");
    }

    /**
     * Displays logs side bar when "Captures" label is clicked.
     */
    public void displayLogs() {
        rightSideMenu.getChildren().clear();
        rightSideMenu.getChildren().add(logBox);
        logsLabel.setStyle("-fx-border-width:  0 0 2 0; -fx-border-color: white; -fx-font-weight: 900");
        capturesLabel.setStyle("-fx-border-width:  0 0 0 0; -fx-font-weight: normal; -fx-opacity: 0.75");
    }


    private void addLogMessage(LocalDateTime time, String message, boolean mask) {
        Label captureTimeLabel = new Label(DatetimeUtils.extractTime(time));
        captureTimeLabel.setTextFill(Paint.valueOf("#458ba8"));
        captureTimeLabel.setStyle("-fx-padding: 0 0 1 0");
        captureTimeLabel.setFont(latoRegular_16);

        Label actionLabel = new Label(message);
        actionLabel.setTextFill(Paint.valueOf("white"));
        actionLabel.setFont(latoRegular_16);

        VBox log = new VBox(captureTimeLabel, actionLabel);
        log.setStyle("-fx-padding: 12px 11px 12px 15px");
        VBox holder = new VBox(log);

        if (mask) {
            captureTimeLabel.setTextFill(Paint.valueOf("#e85467"));
            log.setBackground(new Background(new BackgroundFill(Paint.valueOf("#7c0504"), null, null)));
            holder.setStyle("-fx-border-color: #c90301; -fx-border-width: 0px 0px 0px 3px");
        }
        if (message.startsWith("ERROR")) {
            captureTimeLabel.setTextFill(Paint.valueOf("#FFD180"));
            log.setBackground(new Background(new BackgroundFill(Paint.valueOf("#EF6C00"), null, null)));
            holder.setStyle("-fx-border-color: #BF360C; -fx-border-width: 0px 0px 0px 3px");
        }

        logBox.getChildren().add(0, holder);
        // show only the newest 500 records
        if (logBox.getChildren().size() > 500) {
            logBox.getChildren().remove(500);
        }
    }

    private void loadCaptures() {
        File maskDir = new File(Settings.masksDir());
        if (maskDir.exists()) {
            File[] files = (maskDir).listFiles();
            Arrays.sort(files, (f1, f2) -> Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()));

            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(Settings.imageFormat())) {
                    loadCapture(file, false);
                }
            }
        }
    }

    private void loadCapture(File file, Boolean isMask) {
        Image image = new Image("file:" + file.getAbsolutePath(), 270, 220, true, true);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        LocalDateTime fileDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        Label captureTimeLabel = new Label(DatetimeUtils.extractDatetime(fileDate));
        captureTimeLabel.setTextFill(Paint.valueOf("#ffffff"));
        captureTimeLabel.setFont(latoRegular_14);
        captureTimeLabel.setOpacity(0.75);
        Label captureLabel = new Label(String.valueOf(captureIndex));
        captureIndex++;
        captureLabel.setFont(latoRegular_14);
        captureLabel.setTextFill(Paint.valueOf("#ffffff"));
        captureLabel.setOpacity(0.75);
        ImageView captImageView = new ImageView(cameraIcon);
        captImageView.setFitWidth(13.8);
        captImageView.setFitHeight(8.0);
        captImageView.setOpacity(0.6);

        GridPane gridpane = new GridPane();
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPercentWidth(70);
        ColumnConstraints iconColumn = new ColumnConstraints();
        iconColumn.setPercentWidth(15);
        ColumnConstraints countColumn = new ColumnConstraints();
        countColumn.setPercentWidth(15);
        gridpane.getColumnConstraints().addAll(timeColumn, iconColumn, countColumn);

        gridpane.add(captureTimeLabel, 0, 0);
        gridpane.add(captImageView, 1, 0);
        gridpane.add(captureLabel, 2, 0);
        captureLabel.setPadding(new Insets(0, 3, 0, 0));
        GridPane.setHalignment(captureLabel, HPos.RIGHT);
        GridPane.setHalignment(captImageView, HPos.RIGHT);
        gridpane.setStyle("-fx-padding: 3px 4px 6px 6px; -fx-border-insets: 5px");

        VBox capture = new VBox(imageView, gridpane);
        capture.setAlignment(Pos.CENTER);

        VBox slot = new VBox(capture);
        slot.setAlignment(Pos.CENTER);
        slot.setStyle("-fx-padding: 5px; -fx-border-insets: 5px");

        if (isMask) {
            slot.setBackground(new Background(new BackgroundFill(Paint.valueOf("#b70503"), null, null)));
            slot.setAccessibleText("lastAdded");
            captureTimeLabel.setTextFill(Paint.valueOf("#dfeeae"));
            captureLabel.setTextFill(Paint.valueOf("#dfeeae"));
            Thread removeHighlight = new Thread(() -> {
                try {
                    Thread.sleep(alarmHighlightDuration.get());
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                Platform.runLater(() -> {
                    slot.setBackground(new Background(new BackgroundFill(Paint.valueOf("#12212f"), null, null)));
                    slot.setAccessibleText(null);
                    captureTimeLabel.setTextFill(Paint.valueOf("white"));
                    captureLabel.setTextFill(Paint.valueOf("white"));
                    captureTimeLabel.setOpacity(0.75);
                    captureLabel.setOpacity(0.75);
                });
            });
            removeHighlight.setDaemon(true);
            removeHighlight.start();
        }

        captureBox.getChildren().add(0, slot);
        // show only the newest 10 records
        if (captureBox.getChildren().size() > 10) {
            captureBox.getChildren().remove(10);
        }
    }

    private void alert() {
        if (!alertingInProgress.get()) {
            alertingInProgress.set(true);
            Runnable alertingRunnable = () -> {
                Platform.runLater(() -> {
                    if (settingsMenu.isVisible()) {
                        settingsMenu.close();
                    }
                    rootPane.setStyle(AlarmColors.getCurrentStyle());
                    captureBox.getChildren().forEach(node -> {
                        if (node instanceof VBox) {
                            if (node.getAccessibleText() == null) {
                                ((VBox) node).setBackground(new Background(
                                        new BackgroundFill(Paint.valueOf("#431219"), null, null)));
                            }
                        }
                    });
                    dartLogo.setImage(logoAlarm);
                    digitalClock.setTextFill(Paint.valueOf("white"));
                    settings.setImage(alertSettingIcon);
                    alertLabel.setVisible(true);
                });
                try {
                    Thread.sleep(AppController.alarmDuration.get());
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                Platform.runLater(() -> {
                    rootPane.setStyle("-fx-background-color:#060918");
                    captureBox.getChildren().forEach(node -> {
                        if (node instanceof VBox) {
                            if (node.getAccessibleText() == null) {
                                ((VBox) node).setBackground(new Background(
                                        new BackgroundFill(Paint.valueOf("#12212f"), null, null)));
                            }
                        }
                    });
                    dartLogo.setImage(logo);
                    digitalClock.setTextFill(Paint.valueOf("#91dbf8"));
                    settings.setImage(settingsMenu.isVisible() ? activeSettingsIcon : settingsIcon);
                    alertLabel.setVisible(false);
                    alertingInProgress.set(false);
                });
            };

            Thread alertingThread = new Thread(alertingRunnable);
            alertingThread.setDaemon(true);
            alertingThread.start();
            alertCall.alert();
        }
    }

    public File saveMask(String fileName) {
        File file = new File(fileName);
        String masksDir = Settings.masksDir();
        File picsDir = new File(masksDir);
        if (!picsDir.isDirectory()) {
            picsDir.mkdir();
        }
        File maskFile = new File(picsDir.getAbsolutePath() + File.separator + System.currentTimeMillis() + "." + Settings.imageFormat());
        file.renameTo(maskFile);
        return maskFile;
    }

    private class GuiProxyImpl implements GuiProxy {

        private void writeMessage(String message, boolean mask) {
            Platform.runLater(() -> {
                logger.info(message);
                addLogMessage(LocalDateTime.now(), message, mask);
            });
        }

        @Override
        public void writeMessage(String message) {
            writeMessage(message, false);
            if (SettingsController.getEnableCaptureIndicator() && message.equals("Capturing an image")) {
                processingLabel.setVisible(true);
                try {
                    Thread.sleep(CAPTURE_BLINK_LENGTH_MILLIS);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                Platform.runLater(() -> processingLabel.setVisible(false));
            }
        }

        @Override
        public void maskDetected(String message, String file) {
            writeMessage(message, true);
            File savedMask = saveMask(file);
            Platform.runLater(() -> loadCapture(savedMask, true));
            alert();
        }

        @Override
        public Webcam getWebCam() {
            return videoController.getWebCam();
        }

    }

}
