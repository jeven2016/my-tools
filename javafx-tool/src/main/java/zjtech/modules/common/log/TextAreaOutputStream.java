package zjtech.modules.common.log;

import java.io.IOException;
import java.io.OutputStream;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextAreaOutputStream extends OutputStream {

  private TextArea textArea;

  public TextAreaOutputStream(TextArea textArea) {
    this.textArea = textArea;
  }

  @Override
  public void write(int b) {
    //because another thread pool will log something, this behaviour will trigger
    //appending log messages to javafx component
    Platform.runLater(() -> textArea.appendText(String.valueOf((char) b)));

  }
}
