package com.camera.util;

import com.github.sarxos.webcam.Webcam;

public interface GuiProxy {
    public void writeMessage(String message);
    public void maskDetected(String message, String file);
    public Webcam getWebCam();
}
