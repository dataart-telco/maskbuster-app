package com.camera.controller;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camera.fx.CustomImageView;
import com.camera.thread.CapturingThread;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

public class VideoController extends VBox {
	
	private final Logger logger = LoggerFactory.getLogger(VideoController.class);

    private final ImageView videoImageView;
    private Webcam webCam;
    private final ObjectProperty<Image> imageProperty;
	
    public VideoController() {
		this.videoImageView = new CustomImageView();
		this.imageProperty = new SimpleObjectProperty<Image>();
		 FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/video.fxml"));
         fxmlLoader.setRoot(this);
         fxmlLoader.setController(this);
        
         try {
             fxmlLoader.load();
         } catch (IOException exception) {
             throw new RuntimeException(exception);
         }
	}

	@FXML
	public void initialize(){
        this.setAlignment(Pos.CENTER);
		this.getChildren().add(videoImageView);
		initializeWebCam(0);
	}
	
	protected void setImageViewSize() {

		double height = this.getHeight();
		double width = this.getWidth();
		int scaleFactor = 1;

		videoImageView.setFitHeight(Math.floor(height * scaleFactor));
		videoImageView.setFitWidth(Math.floor(width * scaleFactor));
		videoImageView.prefHeight(Math.floor(height * scaleFactor));
		videoImageView.prefWidth(Math.floor(width * scaleFactor));
		videoImageView.setPreserveRatio(true);

	}
	
	public void initializeWebCam(final int webCamIndex) {

		Task<Void> webCamTask = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				if (webCam != null) {
					disposeWebCamCamera();
				}

				if (webCamIndex == -1) {
					webCam = Webcam.getDefault();
				} else {
					webCam = Webcam.getWebcams().get(webCamIndex);

				}
				webCam.setCustomViewSizes(new Dimension[] { WebcamResolution.HD720.getSize() }); 	// size
				webCam.setViewSize(WebcamResolution.HD720.getSize());
				webCam.open();
				startWebCamStream();
				return null;
			}
		};
		Thread webCamThread = new Thread(webCamTask);
		webCamThread.setDaemon(true);
		webCamThread.start();

	}
	
	protected void disposeWebCamCamera() {
		webCam.close();
	}
	
	protected void startWebCamStream() {

		Platform.runLater(this::setImageViewSize);

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				final AtomicReference<WritableImage> ref = new AtomicReference<>();
				BufferedImage img = null;

				while (true) {
					try {
						
						if(!SettingsController.getEnableVideoStreaming()){
							img = CapturingThread.getCapturedImage();
						}else{
							img = webCam.getImage();
						}
						
						if ( img != null) {
							ref.set(SwingFXUtils.toFXImage(img, ref.get()));
							img.flush();
							Platform.runLater(() -> imageProperty.set(ref.get()));
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		};

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();
		videoImageView.imageProperty().bind(imageProperty);

	}

	public Webcam getWebCam() {
		return webCam;
	}

	public void setWebCam(Webcam webCam) {
		this.webCam = webCam;
	}

	
}
