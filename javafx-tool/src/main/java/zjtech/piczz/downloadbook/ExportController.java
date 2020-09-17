package zjtech.piczz.downloadbook;

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
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.DialogUtils;
import zjtech.modules.utils.InfoUtils;

//refer to : https://www.baeldung.com/jackson-xml-serialization-and-deserialization
@Component
@Slf4j
public class ExportController extends AbstractController {

  @FXML
  public TextField directoryInput;

  private final DialogUtils dialogUtils;

  private final BookService bookService;

  private TextFlow infoArea;

  private final InfoUtils infoUtils;

  private Window window;

  @Autowired
  public ExportController(DialogUtils dialogUtils, BookService bookService,
      InfoUtils infoUtils) {
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.infoUtils = infoUtils;
  }

  public void chooseDir(ActionEvent actionEvent) {
    Stage stage = getCurrentStage(actionEvent);
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle(getResource("dialog.book.choose.dir"));

    File file = directoryChooser.showDialog(stage);
    if (file == null) {
      return;
    }
    if (!file.isDirectory()) {
      dialogUtils.alert("Invalid directory", getResource("dialog.book.choose.dir.content"));
      return;
    }
    directoryInput.setText(file.getAbsolutePath());
  }

  public void setInfoArea(TextFlow infoArea) {
    this.infoArea = infoArea;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  public void perform() throws IOException {
    boolean isDir = Paths.get(directoryInput.getText()).toFile().isDirectory();
    if (!isDir) {
      dialogUtils.alert("Invalid directory", getResource("dialog.book.choose.dir.content"));
      return;
    }
    XmlMapper xmlMapper = new XmlMapper();
    File outputFile = new File(directoryInput.getText() + "/books.xml");

    List<SingleBookEntity> bookList = bookService.findAll();
    bookList.forEach(book -> book.getPictures().clear());
    xmlMapper.writeValue(outputFile, bookList);
    Text text = new Text(getResource("success.book.export"));

    //close dialog
    window.hide();

    //show result
    infoUtils.showInfo(infoArea, InfoUtils.InfoType.SUCCESS, text);

  }
}
