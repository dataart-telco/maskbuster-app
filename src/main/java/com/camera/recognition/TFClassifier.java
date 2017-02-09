package com.camera.recognition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camera.util.GuiProxy;
import com.camera.util.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TFClassifier implements Classifier {
    private HttpClient httpclient = HttpClientBuilder.create().build();
    private ObjectMapper mapper = new ObjectMapper();
    private Logger logger = LoggerFactory.getLogger(TFClassifier.class);
    private GuiProxy guiProxy;

    public TFClassifier(GuiProxy guiProxy) {
        this.guiProxy = guiProxy;
    }

    @Override
    public double detect(String filename) {
        try {
            HttpPost postRequest = new HttpPost("http://" + Settings.tensorflowAppServer() + "/visual-recognition/classify");

            FileBody bin = new FileBody(new File(filename));
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("file", bin)
                    .build();

            postRequest.setEntity(reqEntity);

            postRequest.addHeader("api-key", Settings.tensorflowApiKey());
            HttpResponse response = httpclient.execute(postRequest);

            InputStream in = response.getEntity().getContent();
            String body = IOUtils.toString(in, "UTF-8");
            logger.info(body);

            List<Map<String, Double>> results = mapper.readValue(body, ArrayList.class);
            for (Map<String, Double> result: results) {
                if ("ski mask".equals(result.get("name"))) {
                    return result.get("score");
                }
            }
            return 0.0;

        }catch(IOException e ) {
            if (e instanceof SocketException) {
                guiProxy.writeMessage("ERROR: classifier service error");
            } else if (e instanceof JsonParseException) {
                guiProxy.writeMessage("ERROR: bad classifier response");
            } else if (e instanceof UnknownHostException) {
                guiProxy.writeMessage("ERROR: classifier unknown host");
            }
            logger.error("", e);
            return 0.0;
        }
    }
}
