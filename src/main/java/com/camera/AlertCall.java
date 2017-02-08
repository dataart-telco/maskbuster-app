package com.camera;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.camera.controller.SettingsController;
import com.camera.util.GuiProxy;
import com.camera.util.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
public class AlertCall {
	
	private final Logger logger = LoggerFactory.getLogger(AlertCall.class);
    private HttpClient client = HttpClientBuilder.create().build();
    private final GuiProxy guiProxy;
    
    public AlertCall(GuiProxy guiProxy) {
		this.guiProxy = guiProxy;
	}

    public void alert() {
    	//the idea is to call the number and turn on the light in the same time.
    	Thread callThread = new Thread(this::callNumber);
    	callThread.run();
    	Thread lightSignal = new Thread(this::lightSignal);
    	lightSignal.setDaemon(true);
    	lightSignal.start();
    	
    	try {
			callThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
    }
    
    private void callNumber(){
        String body = "";
        try {
            HttpPost post = new HttpPost(String.format("http://" + Settings.restcommServer() + "/restcomm/2012-04-24/Accounts/ACae6e420f425248d6a26948c17a9e2acf/Calls.json"));

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("From", Settings.callFrom()));
            urlParameters.add(new BasicNameValuePair("To", "client:" + Settings.restcommClient()));
            urlParameters.add(new BasicNameValuePair("Url", "http://" + Settings.restcommServer() + "/restcomm-rvd/services/apps/AP5bbdd3fe899647949f2daad06d0d7af0/controller")); //http://185.76.104.82:8080/restcomm/demos/hello-play.xml

            String auth = Settings.restcommUser() + ":" + Settings.restcommPassword();
            byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("ISO-8859-1")));
            post.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + new String(encodedAuth));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            InputStream in = response.getEntity().getContent();
            body = IOUtils.toString(in, "UTF-8");
            logger.info("Call response"+body);

        } catch (IOException e) {
        	guiProxy.writeMessage("ERROR: Can not call the number.");
        	logger.error(e.getMessage(), e);
        }
    }
    
    private void lightSignal(){
        String body = "";
        try {
            HttpPost post = new HttpPost(Settings.lightServiceUrl());
            post.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + Settings.lightServiceKey());
            post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> rqMap = new HashMap<String, String>(); 
            rqMap.put("0", "100");
            rqMap.put("frequency", "1");
            rqMap.put("count", String.valueOf(SettingsController.getLightTime()));
            String jsonString = mapper.writeValueAsString(rqMap);
            logger.info("Light request "+jsonString);
            post.setEntity(new StringEntity(jsonString));

            HttpResponse response = client.execute(post);
            InputStream in = response.getEntity().getContent();
            body = IOUtils.toString(in, "UTF-8");
            logger.info("Light response"+body);
            
        } catch (IOException e) {
        	guiProxy.writeMessage("ERROR: Can not turn the light on.");
        	logger.error(e.getMessage(), e);
        }
    }
    
}
