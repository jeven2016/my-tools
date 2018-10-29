package zjtech.piczz.downloadbook;

import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.DialogUtils;

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

  @Autowired
  public ImportExportController(DialogUtils dialogUtils) {
    this.dialogUtils = dialogUtils;
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
    directoryInput.setText(file.getAbsolutePath().toString());


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

  public void perform(ActionEvent actionEvent) {

  }

  public enum Type {
    IMPORT, EXPORT;
  }

}
