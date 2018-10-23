/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.CustomJobParameter;
import zjtech.modules.common.DialogUtils;
import zjtech.piczz.common.DownloadConstants;

@Component
@Slf4j
public class DownloadBookController extends AbstractController {

  @FXML
  public TextField bookUrlInput;

  @FXML
  public Button addBookBtn;

  @FXML
  public TableView<SingleBookEntity> tableView;

  @FXML
  public Button refreshBtn;

  @FXML
  public Button startBtn;

  @FXML
  public Button stopBtn;

  private final DialogUtils dialogUtils;

  private final BookService bookService;

  final
  JobLauncher jobLauncher;

  final
  Job job;

  @Autowired
  public DownloadBookController(DialogUtils dialogUtils, BookService bookService,
                                JobLauncher jobLauncher, @Qualifier("downloadSingleBookJob") Job job) {
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.jobLauncher = jobLauncher;
    this.job = job;
  }


  /**
   * Add a book by entering the link
   */
  public void addBook() {
    String link = bookUrlInput.getText();
    if (StringUtils.isEmpty(link)) {
      dialogUtils.alert(getResource("error.invalid.book.title"),
          getResource("error.invalid.book.content"));

      return;
    }
    SingleBookEntity singleBookEntity = new SingleBookEntity();
    singleBookEntity.setUrl(link);
    bookService.save(singleBookEntity);

    bookUrlInput.clear();
    refresh();
  }

  public void delete() {
    long id = tableView.getSelectionModel().getSelectedItem().getId();
    Optional<ButtonType> opt = dialogUtils.confirm(getResource("confirm.book.delete.title"),
        getResource("confirm.book.delete.content"));
    if (opt.isPresent() && opt.get() == ButtonType.OK) {
      bookService.delete(id);
      refresh();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    refresh();
  }

  public void refresh() {
    List<SingleBookEntity> list = bookService.findAll();
    ObservableList<SingleBookEntity> observableList = FXCollections.observableArrayList(list);
    tableView.setItems(observableList);
  }

  public void triggerAdd(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
      this.addBook();
    }
  }

  /**
   * Start a task
   */
  public void start() {
    SingleBookEntity selectedBook = tableView.getSelectionModel().getSelectedItem();
    if (selectedBook == null) {
      dialogUtils
          .alert(getResource("error.book.start.title"), getResource("error.book.start.content"));
      return;
    }

    CustomJobParameter<SingleBookEntity> customJobParameter = new CustomJobParameter<SingleBookEntity>(
        selectedBook);

    //Launch a task to download one book
    JobParameters parameters = new JobParametersBuilder()
        .addParameter(DownloadConstants.SINGLE_BOOK_PARAM, customJobParameter).toJobParameters();
    try {
      ExitStatus status = jobLauncher.run(job, parameters).getExitStatus();
      String msg = getResource("error.book.start.status");
      msg = String.format(msg, status.getExitCode());
      dialogUtils.info(null, msg);
    } catch (Exception e) {
      dialogUtils.alertException(getResource("error.book.start.failed"), e);
      log.warn("failed to launch downloading task", e);
    }

  }
}
