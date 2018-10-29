/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.home;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.GlobalView;
import zjtech.modules.common.log.TextAreaOutputStream;
import zjtech.modules.common.log.TextAreaOutputStreamAppender;

@Component
public class HomeController extends AbstractController {

  private final GlobalView globalView;

  @FXML
  private AnchorPane navPane;

  @FXML
  private AnchorPane contentPane;

  @FXML
  private TextArea textArea;


  @Autowired
  public HomeController(GlobalView globalView) {
    this.globalView = globalView;
  }

  @FXML
  public void initialize(URL location, ResourceBundle resources) {
    globalView.setNavPane(navPane);
    globalView.setContentPane(contentPane);

    //add context menu for textarea
    ContextMenu contextMenu = getContextMenu();
    textArea.setContextMenu(contextMenu);

    //register the appender to show log in GUI
    Platform.runLater(() -> {
      TextAreaOutputStream stream = new TextAreaOutputStream(textArea);

      TextAreaOutputStreamAppender.setStaticOutputStream(stream);
    });
  }

  private ContextMenu getContextMenu() {
    MenuItem clearItem = new MenuItem("Clear");
    clearItem.setOnAction(event -> {
      textArea.clear();
    });

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(clearItem);
    return contextMenu;
  }

  public void logout(ActionEvent actionEvent) {
    Stage stage = getCurrentStage(actionEvent);
    Parent parent = loadParent(FxmlPath.login.value());
    stage.getScene().setRoot(parent);

  }
}
