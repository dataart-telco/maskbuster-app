package com.camera.recognition;

import com.camera.util.Settings;
import com.camera.util.GuiProxy;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;

import java.io.File;

public class WatsonClassifier implements Classifier {
    private VisualRecognition service;
    private GuiProxy writer;

    public WatsonClassifier(GuiProxy writer) {
        service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_19);
        service.setApiKey(Settings.apiKey());
        this.writer = writer;
    }

    @Override
    public double detect(String filename) {
        for (int i=0; i < 3; i++) {
            try {
                ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
                        .images(new File(filename)) // "capture/004.jpeg"
                        .classifierIds(Settings.classifierId())
                        .build();

                VisualClassification classification = service.classify(options).execute();
                writer.writeMessage(classification.toString());

                if (!classification
                        .getImages().iterator().next()
                        .getClassifiers().isEmpty()) {
                    for (VisualClassifier.VisualClass clazz : classification
                            .getImages().iterator().next()
                            .getClassifiers().iterator().next()
                            .getClasses()) {
                        if (clazz.getName().contains("backpack")) {
                            writer.writeMessage("BACKPACK!");
                            return clazz.getScore();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                if (!(e instanceof com.ibm.watson.developer_cloud.service.exception.BadRequestException)) {
                    break;
                }
            }
        }
        return 0.00;
    }
}
