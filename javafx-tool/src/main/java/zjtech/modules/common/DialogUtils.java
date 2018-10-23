/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.modules.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Refer to https://code.makery.ch/blog/javafx-dialogs-official/
 */
@Component
public class DialogUtils {


  public void alert(String title, String message) {
    Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setHeaderText(message);
    alert.show();
  }

  public Optional<ButtonType> info(String title, String message) {
    if (StringUtils.isEmpty(title)) {
      title = ResourceLocater.getBundle().getString("dialog.info.title");
    }
    Alert alert = new Alert(AlertType.INFORMATION);
    return showDialog(title, message, alert);
  }

  public Optional<ButtonType> confirm(String title, String message) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    return showDialog(title, message, alert);
  }


  public Optional<ButtonType> error(String title, String message) {
    Alert alert = new Alert(AlertType.ERROR);
    return showDialog(title, message, alert);
  }

  public Optional<ButtonType> alertException(String message, Throwable ex) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(ResourceLocater.getBundle().getString("error.global.exception.dialog.title"));
    alert.setHeaderText(ResourceLocater.getBundle().getString("error.global.exception.title"));
    alert.setContentText(message);

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    String exceptionText = sw.toString();

    Label label = new Label(
        ResourceLocater.getBundle().getString("error.global.exception.content"));

    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

    alert.getDialogPane().setExpandableContent(expContent);
    return alert.showAndWait();
  }

  private Optional<ButtonType> showDialog(String title, String message, Alert alert) {
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setHeaderText(message);
    return alert.showAndWait();
  }

}
