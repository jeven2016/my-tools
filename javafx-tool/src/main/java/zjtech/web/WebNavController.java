package zjtech.web;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.GlobalView;

@Component
public class WebNavController extends AbstractController {
  private static final String WS_CLIENT_BTN="wsClientBtn";

  @Autowired
  private GlobalView globalView;

  public void showContent(ActionEvent actionEvent) {
    String id = ((Node) actionEvent.getSource()).getId();
    switch (id) {
      case WS_CLIENT_BTN:
        showWsClientPanel();
        break;
    }
  }

  private void showWsClientPanel() {
    globalView.getContentPane().getChildren().setAll(loadParent(FxmlPath.webSocketNav.value()));
  }

}
