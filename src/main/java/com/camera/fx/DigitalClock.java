package com.camera.fx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Calendar;

public class DigitalClock extends Label {
    public DigitalClock() {
        setTextFill(Paint.valueOf("#91dbf8"));
        Font font = Font.loadFont(getClass().getResourceAsStream("/fonts/Lato-Light.ttf"), 48);
        setFont(Font.font(font.getFamily(), FontWeight.THIN, 48D));
        setAlignment(Pos.BOTTOM_LEFT);
        bindToTime();
    }

    /**
     * Creates a string left padded to the specified width with the supplied
     * padding character.
     *
     * @param fieldWidth the length of the resultant padded string.
     * @param padChar    a character to use for padding the string.
     * @param s          the string to be padded.
     * @return the padded string.
     */
    public static String pad(int fieldWidth, char padChar, String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < fieldWidth; i++) {
            sb.append(padChar);
        }
        sb.append(s);

        return sb.toString();
    }

    // the digital clock updates once a second.
    private void bindToTime() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), actionEvent -> {
            Calendar time = Calendar.getInstance();
            String hourString = pad(2, ' ',
                    time.get(Calendar.HOUR_OF_DAY) + "");
            String minuteString = pad(2, '0', time.get(Calendar.MINUTE) + "");
            String secondString = pad(2, '0', time.get(Calendar.SECOND) + "");
            setText(hourString + ":" + minuteString + ":" + secondString);
        }), new KeyFrame(Duration.seconds(1)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
