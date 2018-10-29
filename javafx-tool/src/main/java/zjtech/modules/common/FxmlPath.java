/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

public enum FxmlPath {
  login("/fxml/login.fxml"),
  home("/fxml/home.fxml"),

  picNav("/fxml/pic/nav.fxml"),
  globalSetting("/fxml/pic/global_setting.fxml"),
  downloadBook("/fxml/pic/download_book.fxml"),
  exportImportDialog("/fxml/pic/import_export_dialog.fxml");

  private String value;

  FxmlPath(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

}
