/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.GlobalView;

@Component
public class NavController extends AbstractController {

  private static final String GLOBAL_SETTING_BTN = "globalSettingBtn";
  private static final String DOWNLOAD_BTN = "downloadBtn";
  private static final String LOG_BTN = "logBtn";

  @Autowired
  private GlobalView globalView;

  public void showContent(ActionEvent actionEvent) {
    String id = ((Node) actionEvent.getSource()).getId();
    switch (id) {
      case GLOBAL_SETTING_BTN:
        showGlobalSettingContent();
        break;
      case DOWNLOAD_BTN:
        showDownloadBookContent();
        break;
    }
  }

  private void showDownloadBookContent() {
    globalView.getContentPane().getChildren().setAll(loadParent(FxmlPath.downloadBook.value()));
  }

  private void showGlobalSettingContent() {
    globalView.getContentPane().getChildren().setAll(loadParent(FxmlPath.globalSetting.value()));
  }
}
