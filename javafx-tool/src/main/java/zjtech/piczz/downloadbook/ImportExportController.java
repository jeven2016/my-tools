package zjtech.piczz.downloadbook;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
public class ImportExportController extends AbstractController {

  @FXML
  public Button performBtn;

  @FXML
  public TextField directoryInput;

  private Type type;

  private final DialogUtils dialogUtils;

  private final BookService bookService;

  private TextFlow infoArea;

  private final InfoUtils infoUtils;

  private Window window;

  @Autowired
  public ImportExportController(DialogUtils dialogUtils, BookService bookService,
                                InfoUtils infoUtils) {
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.infoUtils = infoUtils;
  }

  public void chooseDir(ActionEvent actionEvent) {
    Stage stage = getCurrentStage(actionEvent);
    DirectoryChooser directoryChooser = new DirectoryChooser();
    directoryChooser.setTitle("Choose a directory");

    File file = directoryChooser.showDialog(stage);
    if (file == null) {
      return;
    }
    if (!file.isDirectory()) {
      dialogUtils.alert("Invalid directory", "Please choose valid directory!");
      return;
    }
    directoryInput.setText(file.getAbsolutePath());
  }


  public void setType(Type type) {
    this.type = type;
    if (type.equals(Type.IMPORT)) {
      performBtn.setText("Import Now");
    }
    if (type.equals(Type.EXPORT)) {
      performBtn.setText("Export Now");
    }
  }

  public void setInfoArea(TextFlow infoArea) {
    this.infoArea = infoArea;
  }

  public void setWindow(Window window) {
    this.window = window;
  }

  public void perform(ActionEvent actionEvent) throws IOException {
    if (type.equals(Type.IMPORT)) {
    }
    if (type.equals(Type.EXPORT)) {
      List<SingleBookEntity> bookList = bookService.findAll();
      bookList.forEach(book -> book.getPictures().clear());

      XmlMapper xmlMapper = new XmlMapper();
      File outputFile = new File(directoryInput.getText() + "/exportedBooks.xml");
      xmlMapper.writeValue(outputFile, bookList);

      //close dialog
      window.hide();

      //show result
      infoUtils.showInfo(infoArea, InfoUtils.InfoType.SUCCESS, new Text("Xml file is exported."));
    }

  }

  public enum Type {
    IMPORT, EXPORT;
  }

}
