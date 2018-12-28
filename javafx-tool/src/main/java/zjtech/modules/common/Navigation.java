/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Slf4j
public class Navigation {

  @Autowired
  private FxmlUtils fxmlUtils;

  private Stage stage;
  private Scene scene;

  public void init(Stage stage) {
    this.stage = stage;
    scene = new Scene(new Pane());
    stage.setScene(scene);
  }

  public NavController loadController(String path) {
    try {
      //loads the fxml file
      LoaderEntity loaderEntity = fxmlUtils.loadFxml(path);

      NavController navController = loaderEntity.getFxmlLoader().getController();
      navController.setView(loaderEntity.getParent());
      return navController;
    } catch (Exception e) {
      log.warn("Failed to load controller with {}", path);
    }
    return null;
  }

  public void Show(NavController navController) {
    try {
      scene.setRoot((Parent) navController.getView());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}