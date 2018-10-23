/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.home;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.GlobalView;

@Component
public class HomeController extends AbstractController {

  private final GlobalView globalView;

  @FXML
  private AnchorPane navPane;

  @FXML
  private AnchorPane contentPane;


  @Autowired
  public HomeController(GlobalView globalView) {
    this.globalView = globalView;
  }

  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    globalView.setNavPane(navPane);
    globalView.setContentPane(contentPane);
  }

  public void logout(ActionEvent actionEvent) {
    Stage stage = getCurrentStage(actionEvent);
    Parent parent = loadFxml(FxmlPath.login.value());
    stage.getScene().setRoot(parent);

  }
}
