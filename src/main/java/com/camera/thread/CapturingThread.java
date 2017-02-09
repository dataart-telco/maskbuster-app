package com.camera.thread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camera.controller.SettingsController;
import com.camera.recognition.Classifier;
import com.camera.recognition.TFClassifier;
import com.camera.util.GuiProxy;
import com.camera.util.Settings;

public class CapturingThread extends Thread {
	private final Logger logger = LoggerFactory.getLogger(CapturingThread.class);
	private GuiProxy guiProxy;
	private Classifier classifier;
	private static BufferedImage capturedImage = null;

	public CapturingThread(GuiProxy guiProxy) {
		this.guiProxy = guiProxy;
		this.classifier = new TFClassifier(guiProxy);
		this.setDaemon(true);
	}


	private File saveImage() {
		File file = new File("capture." + Settings.imageFormat());
		try {
			if (guiProxy.getWebCam() != null && guiProxy.getWebCam().isOpen()) {
				capturedImage = guiProxy.getWebCam().getImage();
                if (capturedImage != null) {
                    ImageIO.write(capturedImage, Settings.imageFormat(), file);
                }
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return file;
	}

	@Override
	public void run() {
		while (true) {
			Thread t = new Thread(() -> {
                guiProxy.writeMessage("Capturing an image");
                File file = saveImage();
                guiProxy.writeMessage("Classifying an image");
                double score = classifier.detect(file.getAbsolutePath());
                if(score > SettingsController.getScoreThreshold()){
                    guiProxy.maskDetected("Ski mask detected!", file.getAbsolutePath());
                }
            });
			t.setDaemon(true);
			t.start();
			
			try {
				Thread.sleep(SettingsController.getCaptureInterval());
				t.join();
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}

	}
	
	public static BufferedImage getCapturedImage(){
		return CapturingThread.capturedImage;
	}

}
