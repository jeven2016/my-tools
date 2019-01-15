/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.home;

import java.util.List;
import java.util.stream.Stream;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.GlobalView;

@Component
public class FeatureListController extends AbstractController {

  private static final String PIC_BTN = "picBtn";
  private static final String MIDDLE_BTN = "middleBtn";
  private final GlobalView globalView;
  @FXML
  private Button picBtn;
  @FXML
  private Button middleBtn;

  @Autowired
  public FeatureListController(GlobalView globalView) {
    this.globalView = globalView;
  }

  private Stream<Button> getButtons() {
    return Stream.of(picBtn, middleBtn);
  }


  public void click(ActionEvent actionEvent) {
    String id = ((Node) actionEvent.getSource()).getId();

    getButtons().forEach(btn -> {
      Node btnNode = btn;
      List<String> styleList = btnNode.getStyleClass();
      if ((btnNode.getId().equals(id))) {
        if (styleList.contains("success")) {
          return;
        }
        styleList.remove("default");
        styleList.add("success");
      } else {
        styleList.remove("success");
        styleList.add("default");
      }
    });

    switch (id) {
      case PIC_BTN:
        showPicPane();
        break;
      case MIDDLE_BTN:
        //web test tool
        showWebPanel();
        break;
    }
  }

  private void showPicPane() {
    Pane navPane = globalView.getNavPane();
    Parent parent = loadParent(FxmlPath.picNav.value());
    navPane.getChildren().setAll((AnchorPane) parent);
  }

  private void showWebPanel() {
    Pane navPane = globalView.getNavPane();
    Parent parent = loadParent(FxmlPath.webNav.value());
    navPane.getChildren().setAll((AnchorPane) parent);
  }

}
