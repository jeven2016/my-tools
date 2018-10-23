/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

import java.io.IOException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FxmlUtils {

  private final ApplicationContext applicationContext;

  @Autowired
  public FxmlUtils(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public LoaderEntity loadFxml(String path, ResourceBundle resources) {
    if (resources == null) {
      resources = ResourceLocater.getBundle();
    }
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
    fxmlLoader.setControllerFactory(applicationContext::getBean);
    fxmlLoader.setResources(resources);
    Parent parent = null;
    try {
      parent = fxmlLoader.load();
    } catch (IOException e) {
      log.warn("Failed to load fxml file", e);
    }
    return new LoaderEntity(fxmlLoader, parent);
  }

  public LoaderEntity loadFxml(String path) {
    return this.loadFxml(path, ResourceLocater.getBundle());
  }


}
