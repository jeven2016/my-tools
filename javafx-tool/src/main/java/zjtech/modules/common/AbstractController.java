/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@NoArgsConstructor
public abstract class AbstractController implements Initializable {


  @Autowired
  private FxmlUtils fxmlUtils;

  protected Parent loadParent(String path) {
    return fxmlUtils.loadFxml(path).getParent();
  }

  protected LoaderEntity load(String path) {
    return fxmlUtils.loadFxml(path);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  protected Stage getCurrentStage(ActionEvent actionEvent) {
    return (Stage) (((Node) actionEvent.getSource()).getScene().getWindow());
  }

  protected String getResource(String key) {
    return ResourceLocater.getBundle().getString(key);
  }
}
