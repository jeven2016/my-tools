package zjtech.piczz.downloadbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.DialogUtils;
import zjtech.modules.common.NoArgCallback;
import zjtech.modules.utils.InfoUtils;

@Component
@Slf4j
public class ImportController extends AbstractController {

  private final DialogUtils dialogUtils;

  private final BookService bookService;

  private TextFlow infoArea;

  private final InfoUtils infoUtils;

  private Window window;

  private NoArgCallback callback;

  @FXML
  public TextField fileInput;

  @Autowired
  public ImportController(DialogUtils dialogUtils, BookService bookService,
      InfoUtils infoUtils) {
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.infoUtils = infoUtils;
  }

  public void setInfoArea(TextFlow infoArea) {
    this.infoArea = infoArea;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  public void setCallback(NoArgCallback callback) {
    this.callback = callback;
  }

  public void perform() throws IOException {
    boolean isFile = Paths.get(fileInput.getText()).toFile().isFile();
    if (!isFile) {
      dialogUtils.alert("Invalid directory", getResource("dialog.book.choose.dir.content"));
      return;
    }
    XmlMapper xmlMapper = new XmlMapper();
    File outputFile = new File(fileInput.getText());

    List<SingleBookEntity> list = xmlMapper
        .readValue(outputFile, new TypeReference<List<SingleBookEntity>>() {
        });
    bookService.saveList(list);
    Text text = new Text(getResource("success.book.import"));

    //close dialog
    window.hide();

    //show result
    infoUtils.showInfo(infoArea, InfoUtils.InfoType.SUCCESS, text);

    //refresh the table view
    callback.invoke();
  }

  public void chooseFile(ActionEvent actionEvent) {
    Stage stage = getCurrentStage(actionEvent);
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(getResource("dialog.book.choose.file"));
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("XML Files", "*.xml"));

    File file = fileChooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    if (!file.isFile()) {
      dialogUtils.alert(getResource("book.info.invalid.file"),
          getResource("dialog.book.choose.file.content"));
      return;
    }
    fileInput.setText(file.getAbsolutePath());
  }
}
