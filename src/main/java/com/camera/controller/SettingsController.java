package com.camera.controller;

import com.camera.WebCamInfo;
import com.camera.util.AlarmColors;
import com.camera.util.Settings;
import com.github.sarxos.webcam.Webcam;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import com.xuggle.ferry.AtomicInteger;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SettingsController {

    private static final int TO_MILLIS_MULTIPLIER = 1000;
    private static AtomicLong captureInterval = new AtomicLong(1000);
    private static AtomicBoolean enableVideoStreaming = new AtomicBoolean(Settings.getLiveStream());
    private static AtomicBoolean enableCaptureIndicator = new AtomicBoolean(Settings.captureIndicator());
    private static AtomicInteger lightTime = new AtomicInteger(Settings.lightServiceCount());
    private static double scoreThreshold = Settings.scoreThreshold();

    @FXML
    private JFXComboBox<WebCamInfo> cameraSelection;
    @FXML
    private JFXComboBox<AlarmColors> colorChoice;
    @FXML
    private JFXSlider captureIntervalSlider;
    @FXML
    private TextField captureIntervalTextField;
    @FXML
    private JFXSlider lightTimeSlider;
    @FXML
    private TextField lightTimeTextField;
    @FXML
    private JFXSlider scoreThresholdSlider;
    @FXML
    private TextField scoreThresholdTextField;
    @FXML
    private JFXSlider alarmDurationSlider;
    @FXML
    private TextField alarmDurationTextField;
    @FXML
    private JFXCheckBox liveStream;
    @FXML
    private JFXSlider alarmHighlightSlider;
    @FXML
    private TextField alarmHighlightTextField;
    @FXML
    private JFXCheckBox captureIndicator;
    private VideoController videoController;

    public SettingsController(VideoController videoController) {
        this.videoController = videoController;
    }

    public static long getCaptureInterval() {
        return captureInterval.get();
    }

    public static boolean getEnableVideoStreaming() {
        return enableVideoStreaming.get();
    }

    public static void setEnableVideoStreaming(Boolean enableVideoStreaming) {
        SettingsController.enableVideoStreaming.set(enableVideoStreaming);
    }

    public static int getLightTime() {
        return lightTime.get();
    }

    public static double getScoreThreshold() {
        return scoreThreshold;
    }

    public static Boolean getEnableCaptureIndicator() {
        return enableCaptureIndicator.get();
    }

    @FXML
    private void initialize() {
        initCameras();
        initColors();
        initCaptureInterval();
        initLightTime();
        initDurationSlider();
        initStreamingOption();
        initAlarmHighLight();
        initScoreThreshold();
        initCaptureIndicator();
    }

    private void initCameras() {
        ObservableList<WebCamInfo> options = FXCollections.observableArrayList();

        int webCamCounter = 0;
        int defaultCamIndex = 0;
        for (Webcam webcam : Webcam.getWebcams()) {
            if (webcam.equals(Webcam.getDefault())) {
                defaultCamIndex = webCamCounter;
            }
            WebCamInfo webCamInfo = new WebCamInfo();
            webCamInfo.setWebCamIndex(webCamCounter);
            webCamInfo.setWebCamName(webcam.getName());
            options.add(webCamInfo);
            webCamCounter++;
        }
        cameraSelection.setItems(options);
        WebCamInfo defaultCamera = new WebCamInfo();
        defaultCamera.setWebCamName(Webcam.getDefault().getName());
        defaultCamera.setWebCamIndex(defaultCamIndex);
        cameraSelection.setValue(defaultCamera);
        cameraSelection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                videoController.initializeWebCam(newValue.getWebCamIndex());
            }
        });
    }

    private void initColors() {
        ObservableList<AlarmColors> colors = FXCollections.observableArrayList();

        colors.add(AlarmColors.RED);
        colors.add(AlarmColors.PINK);
        colors.add(AlarmColors.PURPLE);

        colorChoice.setItems(colors);

        String alarmThemeColor = Settings.alarmColorTheme();
        if (alarmThemeColor != null && !alarmThemeColor.isEmpty()) {
            AlarmColors loaded = AlarmColors.getColorByName(alarmThemeColor);
            AlarmColors.setCurrentStyle(loaded.getColorStyle());
            colorChoice.setValue(loaded);
        } else {
            colorChoice.setValue(AlarmColors.RED);
        }

        colorChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            AlarmColors.setCurrentStyle(newValue.getColorStyle());
        });
    }

    private void initCaptureInterval() {
        Long captureInterval = Settings.captureInterval();
        if (captureInterval != null && captureInterval > 0) {
            captureIntervalSlider.setValue(captureInterval);
            captureIntervalTextField.setText(String.valueOf(captureInterval));
            SettingsController.captureInterval.set(captureInterval * TO_MILLIS_MULTIPLIER);
        }

        captureIntervalSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!captureIntervalSlider.isValueChanging()) {
                captureIntervalTextField.setText(String.valueOf(newValue.intValue()));
                SettingsController.captureInterval.set(newValue.longValue() * TO_MILLIS_MULTIPLIER);
            }
        });
    }

    private void initLightTime() {
        int lightTime = Settings.lightServiceCount();
        if (lightTime > 0) {
            SettingsController.lightTime.set(lightTime);
            lightTimeSlider.setValue(lightTime);
            lightTimeTextField.setText(String.valueOf(lightTime));
        }

        lightTimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!lightTimeSlider.isValueChanging()) {
                lightTimeTextField.setText(String.valueOf(newValue.intValue()));
                SettingsController.lightTime.set(newValue.intValue());
            }
        });
    }

    private void initScoreThreshold() {
        scoreThresholdSlider.setValueFactory(slider ->
                Bindings.createStringBinding(
                        () -> String.valueOf(new DecimalFormat("#.##").format(slider.getValue())),
                        slider.valueProperty())
        );
        double threshold = Settings.scoreThreshold();
        if (threshold > 0) {
            SettingsController.scoreThreshold = threshold;
            scoreThresholdSlider.setValue(threshold);
            scoreThresholdTextField.setText(String.valueOf(threshold));
        }

        scoreThresholdSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!scoreThresholdSlider.isValueChanging()) {
                scoreThresholdTextField.setText(String.valueOf(new DecimalFormat(
                        newValue.doubleValue() == 1.0 ? "#.#" : "#.##"
                ).format(newValue.doubleValue())));
                SettingsController.scoreThreshold = newValue.doubleValue();
            }
        });
    }

    private void initDurationSlider() {
        Long alarmDuration = Settings.alarmDuration();
        if (alarmDuration != null && alarmDuration > 0) {
            alarmDurationSlider.setValue(alarmDuration);
            alarmDurationTextField.setText(String.valueOf(alarmDuration));
            AppController.setAlarmDuration(alarmDuration * TO_MILLIS_MULTIPLIER);
        }

        alarmDurationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!alarmDurationSlider.isValueChanging()) {
                alarmDurationTextField.setText(String.valueOf(newValue.intValue()));
                AppController.setAlarmDuration(newValue.longValue() * TO_MILLIS_MULTIPLIER);
            }
        });
    }

    private void initStreamingOption() {
        liveStream.setSelected(enableVideoStreaming.get());
        liveStream.selectedProperty().addListener((observable, oldValue, newValue) -> enableVideoStreaming.set(newValue));
    }

    private void initAlarmHighLight() {
        Long alarmHighlightDuration = Settings.alarmHighlightDuration();
        if (alarmHighlightDuration != null && alarmHighlightDuration > 0) {
            alarmHighlightSlider.setValue(alarmHighlightDuration);
            alarmHighlightTextField.setText(String.valueOf(alarmHighlightDuration));
            AppController.setAlarmHighlightDuration(alarmHighlightDuration * TO_MILLIS_MULTIPLIER);
        }
        alarmHighlightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!alarmHighlightSlider.isValueChanging()) {
                alarmHighlightTextField.setText(String.valueOf(newValue.intValue()));
                AppController.setAlarmHighlightDuration(newValue.longValue() * TO_MILLIS_MULTIPLIER);
            }
        });
    }

    private void initCaptureIndicator() {
        captureIndicator.setSelected(enableCaptureIndicator.get());
        captureIndicator.selectedProperty().addListener((observable, oldValue, newValue) -> enableCaptureIndicator.set(newValue));
    }

}
