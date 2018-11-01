package zjtech.piczz.downloadbook;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.piczz.downloadbook.threadpool.DownloadingThreadPool;

@Component
@Lazy
@Slf4j
public class PoolController extends AbstractController {

  @FXML
  public Text pictures;

  @FXML
  public Text threadCount;
  private TextFlow infoArea;
  private Window window;

  private final DownloadingThreadPool pool;

  @Autowired
  public PoolController(DownloadingThreadPool pool) {
    this.pool = pool;
  }

  public void refresh(ActionEvent actionEvent) {
    int currentSize = pool.getPoolSize();
    int threadSize = pool.getThreadCount();

    pictures.setText(currentSize + "");
    threadCount.setText(threadSize + "");
  }

  public void setInfoArea(TextFlow infoArea) {
    this.infoArea = infoArea;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
