package com.camera.util;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    private static Properties props = new Properties();

    public static void load() {
        InputStream input = null;

        String userDataVar = System.getenv("SNAP_USER_DATA");
        File userDataFile = new File(((userDataVar != null) ? userDataVar : "") + File.separator + "application.properties");
        try {
            if (userDataVar != null && !userDataVar.isEmpty() && userDataFile.exists()) {
                input = new FileInputStream(userDataFile);
            } else {
                userDataFile = new File("application.properties");
                input = new FileInputStream(userDataFile);
            }

            props.load(input);

        } catch (FileNotFoundException ex) {
            System.out.println("File " + userDataFile.getAbsolutePath() + " doesn't exist");
            //Message.error("File application.properties doesn't exist");
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

    public static String restcommAcount() {
        return props.getProperty("restcomm_account");
    }

    public static String restcommApp() {
        return props.getProperty("restcomm_app");
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

    public static String lightServiceUrl2() {
        return String.valueOf(props.getProperty("light_service_url2"));
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
