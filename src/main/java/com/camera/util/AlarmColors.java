package com.camera.util;

public enum AlarmColors {
    RED("Red", "-fx-background-color: linear-gradient(from 0px -14px to 14px 0px, reflect, #750402 50%, #7c0505 60%)"),
    PINK("Pink", "-fx-background-color: linear-gradient(from 0px -14px to 14px 0px, reflect, #880E4F 50%, #E91E63 60%)"),
    PURPLE("Purple", "-fx-background-color: linear-gradient(from 0px -14px to 14px 0px, reflect, #4A148C 50%, #9C27B0 60%)");

    private static String currentStyle = RED.getColorStyle();
    private String colorName;
    private String colorStyle;

    AlarmColors(String colorName, String colorStyle) {
        this.colorName = colorName;
        this.colorStyle = colorStyle;
    }

    public static String getCurrentStyle() {
        return currentStyle;
    }

    public static void setCurrentStyle(String currentStyle) {
        AlarmColors.currentStyle = currentStyle;
    }

    public String getColorStyle() {
        return colorStyle;
    }

    public static AlarmColors getColorByName(String colorName) {
        for (AlarmColors color : values()) {
            if (color.colorName.equalsIgnoreCase(colorName)) {
                return color;
            }
        }
        return RED;
    }

    @Override
    public String toString() {
        return colorName;
    }
}
