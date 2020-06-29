package com.example.javafx;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class StageListener implements ApplicationListener<StageReadyEvent> {

    private final String applicationTitle;
    private final ApplicationContext applicationContext;
    private static Scene scene;

    @Autowired
    ResourceLoader resourceLoader;

    public StageListener(@Value("${spring.application.ui.title}") String applicationTitle, ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent stageReadyEvent) {
        Stage stage = stageReadyEvent.getStage();
        // create scene
        stage.setTitle(this.applicationTitle);
        scene = new Scene(new Browser(this.resourceLoader), 900, 600, Color.web("#666970"));
        stage.setScene(scene);
        stage.show();
    }
}

class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final ResourceLoader resourceLoader;

    public Browser(ResourceLoader resourceLoader) {
        webEngine.setJavaScriptEnabled(true);
        this.resourceLoader = resourceLoader;
        String html = "";

        URL url = loadResource("html/bootstrap.html");
        try {
            InputStream inputStream = url.openStream();
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            html = new String(bdata, StandardCharsets.UTF_8);
            html = html.replace("{BOOTSTRAP_CSS}", loadResource("css/bootstrap.min.css").toURI().toString());
            html = html.replace("{JUMBOTRON_CSS}", loadResource("css/jumbotron.css").toURI().toString());
            html = html.replace("{BOOTSTRAP_JS}", loadResource("js/bootstrap.min.js").toURI().toString());
            System.out.println(html);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        //apply the styles
        getStyleClass().add("browser");
        // load the web page
        webEngine.loadContent(html);
        //add the web view to the scene
        getChildren().add(browser);
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser,0,0,w,h,0, HPos.CENTER, VPos.CENTER);
    }

    @Override protected double computePrefWidth(double height) {
        return 750;
    }

    @Override protected double computePrefHeight(double width) {
        return 500;
    }

    public URL loadResource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource(resource);
    }
}