package zjtech.modules.utils;

import java.util.stream.Stream;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A utility to show message in TextFlow widget
 */
@Component
@Slf4j
public class InfoUtils {

  public static enum InfoType {
    SUCCESS("alert,alert-success"), FAILURE("alert,alert-danger"), INFO(
        "alert,alert-info"), WARNING("alert,alert-warning");
    private String value;

    InfoType(String value) {
      this.value = value;
    }
  }

  public void showInfo(TextFlow textFlow, InfoType infoType, Node... nodes) {
    textFlow.setVisible(true);
    textFlow.getStyleClass().setAll(infoType.value.split(","));
    Stream.of(nodes).forEach(node -> node.getStyleClass().setAll("strong"));
    textFlow.getChildren().setAll(nodes);

    // hide the textflow in 5 seconds(using Timeline instead of java thread,
    // because only JavaFx Application thread can update UI)
    KeyFrame keyFrame = new KeyFrame(Duration.seconds(5), event -> {
      textFlow.setVisible(false);
    });
    Timeline timeline = new Timeline(keyFrame);
    Platform.runLater(timeline::play);
  }

}
