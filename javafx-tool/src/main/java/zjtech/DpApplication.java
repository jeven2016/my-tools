/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.ResourceLocater;

@SpringBootApplication
@EnableScheduling
@EnableBatchProcessing
@EnableCaching
public class DpApplication extends Application {

  private ConfigurableApplicationContext applicationContext;
  private Parent parent;

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void init() throws Exception {
    //Using global JavaFX style sheets
    //You can use the  Application.setUserAgentStylesheet("/path/to/css") method to apply CSS files
    //to all Scenes in your application simultaneously.
//    Application.setUserAgentStylesheet("/app.css");

    applicationContext = SpringApplication.run(DpApplication.class);
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(FxmlPath.login.value()));
    fxmlLoader.setControllerFactory(applicationContext::getBean);
    fxmlLoader.setResources(ResourceLocater.getBundle());
    parent = fxmlLoader.load();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Scene primaryScene = new Scene(parent);
    primaryScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

    primaryStage.setScene(primaryScene);
    primaryStage.initStyle(StageStyle.DECORATED);
    primaryStage.setFullScreen(false);
    primaryStage.setIconified(true);
    primaryStage.setResizable(true);
    primaryStage.setMaximized(false);
//    primaryStage.setAlwaysOnTop(true);
    primaryStage.setTitle("My Tools");
    primaryStage.show();
  }

  @Override
  public void stop() throws Exception {
    applicationContext.close();
    System.exit(0);
  }
}
