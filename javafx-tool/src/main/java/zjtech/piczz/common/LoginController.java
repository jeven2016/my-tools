/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.common;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;

@Component
public class LoginController extends AbstractController {

  @FXML
  public PasswordField passwordField;

  @FXML
  public TextField userNameField;

  @FXML
  private Button signInBtn;

  @FXML
  private Button cancelBtn;

  @Value("${login.username}")
  private String username;

  @Value("${login.password}")
  private String password;

  public void singIn(ActionEvent actionEvent) {
    Stage stage = (Stage) (((Node) actionEvent.getSource()).getScene().getWindow());
    doSingIn(stage);
  }

  private void doSingIn(Stage stage) {
    String name = userNameField.getText();
    String pwd = passwordField.getText();

    if (!name.equals(username) || !pwd.equals(password)) {
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("Wrong username or password");
      alert.setHeaderText("Your username or password is incorrect, please try again.");
      alert.show();
      return;
    }

    //navigate to next scene
    Scene scene = new Scene(loadParent(FxmlPath.home.value()));
    stage.setScene(scene);
    stage.setResizable(true);
    stage.show();
  }

  public void stop(ActionEvent actionEvent) throws Exception {
    Platform.exit();
  }

  public void triggerSignIn(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
      Stage stage = (Stage) (((Node) keyEvent.getSource()).getScene().getWindow());
      this.doSingIn(stage);
    }
  }
}
