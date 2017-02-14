package com.camera.util;

import javafx.scene.control.Alert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static Properties props = new Properties();

    public static void load() {
        InputStream input = null;
        try {
            input = new FileInputStream("application.properties");
            props.load(input);

        } catch (FileNotFoundException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("File application.properties doesn't exist");
            alert.showAndWait();
            System.out.println("File application.properties doesn't exist");
            System.exit(1);

        } catch (IOException ex) {
            Message.error(ex.getMessage());
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Message.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static String callFrom() {
        return props.getProperty("call_from");
    }

    public static String restcommServer() {
        return props.getProperty("restcomm_server");
    }

    public static String restcommUser() {
        return props.getProperty("restcomm_username");
    }

    public static String restcommPassword() {
        return props.getProperty("restcomm_password");
    }

    public static String restcommClient() {
        return props.getProperty("restcomm_client");
    }

    public static String tensorflowAppServer() {
        return props.getProperty("tensorflow_app_server");
    }

    public static Double scoreThreshold() {
        return Double.parseDouble(props.getProperty("score_threshold", "0.6"));
    }

    public static String tensorflowApiKey() {
        return props.getProperty("tensorflow_app_api_key");
    }
    
    public static String masksDir() {
        return props.getProperty("masks_dir");
    }
    
    public static String imageFormat() {
        return props.getProperty("image_format");
    }

    public static String alarmColorTheme() {
        return props.getProperty("color_theme", "Red");
    }

    public static Long captureInterval() {
        return Long.parseLong(props.getProperty("interval", "1"));
    }

    public static Long alarmDuration() {
        return Long.parseLong(props.getProperty("alarm_duration", "5"));
    }

    public static boolean getLiveStream(){
        return Boolean.parseBoolean(props.getProperty("liveStream"));
    }

    public static Long alarmHighlightDuration() {
        return Long.parseLong(props.getProperty("highlight_duration", "5"));
    }    
    
    public static String lightServiceUrl() {
        return String.valueOf(props.getProperty("light_service_url"));
    }
    
    public static String lightServiceKey() {
        return String.valueOf(props.getProperty("light_service_key"));
    }
    
    public static int lightServiceCount() {
        return Integer.valueOf(props.getProperty("light_service_count"));
    }

    public static Boolean captureIndicator() {
        return "true".equals(String.valueOf(props.getProperty("capture_indicator", "true")));
    }

    public static String getLogPropertiesPath() {
        return props.getProperty("log_file");
    }

    public static String imagePath() {
        return props.getProperty("image_path");
    }

    public static String salesforceToken() {
        return props.getProperty("salesforce_token");
    }

    public static String salesforcePhone() {
        return props.getProperty("salesforce_phone");
    }

    public static String salesforceUrl() {
        return props.getProperty("salesforce_url");
    }
}
