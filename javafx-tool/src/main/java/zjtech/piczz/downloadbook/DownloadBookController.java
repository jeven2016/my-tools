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
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.CustomJobParameter;
import zjtech.modules.common.DialogUtils;
import zjtech.modules.common.FxmlPath;
import zjtech.modules.common.LoaderEntity;
import zjtech.modules.common.NoArgCallback;
import zjtech.modules.common.ToolException;
import zjtech.modules.common.cache.EhcacheUtil;
import zjtech.modules.utils.InfoUtils;
import zjtech.modules.utils.InfoUtils.InfoType;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;

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
  public Button stopBtn;

  private final DialogUtils dialogUtils;

  private final BookService bookService;

  private final JobLauncher jobLauncher;

  private final Job job;

  @FXML
  private TextFlow infoArea;

  private final InfoUtils infoUtils;

  private ApplicationContext applicationContext;

  private final PicRep picRep;

  @Autowired
  public DownloadBookController(ApplicationContext applicationContext, DialogUtils dialogUtils,
                                BookService bookService,
                                @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
                                @Qualifier("downloadSingleBookJob") Job job,
                                InfoUtils infoUtils, PicRep picRep,
                                EhcacheUtil ehcacheUtil) {
    this.applicationContext = applicationContext;
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.jobLauncher = jobLauncher;
    this.job = job;
    this.infoUtils = infoUtils;
    this.picRep = picRep;
    this.ehcacheUtil = ehcacheUtil;
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
    singleBookEntity.setStatus(StatusEnum.NEW_ADDED);
    try {
      bookService.save(singleBookEntity);
    } catch (ToolException e) {
      String resource = getResource(e.getErrorCode().getCode().toString());
      infoUtils.showInfo(infoArea, InfoType.FAILURE, new Text(resource));
      log.info("Failed to insert a duplicated book for url {}", link);
      return;
    }

    bookUrlInput.clear();

    String msg = String.format(getResource("success.book.add"), link);
    infoUtils.showInfo(infoArea, InfoType.SUCCESS, new Text(msg));
    log.info(msg);
    refresh();
  }

  public void delete() {
    SingleBookEntity bookEntity = tableView.getSelectionModel().getSelectedItem();
    if (bookEntity == null) {
      infoUtils.showInfo(infoArea, InfoType.WARNING,
          new Text(getResource("error.book.deletion.failed")));
      log.info("NO book is specified to be deleted.");
      return;
    }
    long id = bookEntity.getId();
    Optional<ButtonType> opt = dialogUtils.confirm(getResource("confirm.book.delete.title"),
        getResource("confirm.book.delete.content"));
    if (opt.isPresent() && opt.get() == ButtonType.OK) {
      bookService.delete(id);

      // show the result
      String msg = String.format(getResource("success.book.delete"), bookEntity.getUrl());
      infoUtils.showInfo(infoArea, InfoType.SUCCESS, new Text(msg));

      log.info("The book (ID={}, URL={}) is deleted successfully.", id, bookEntity.getUrl());
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
    log.info("refresh the list of books");
  }

  public void triggerAdd(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
      this.addBook();
    }
  }

  /**
   * Start task(s)
   */
  public void start() {
    SingleBookEntity selectedBook = tableView.getSelectionModel().getSelectedItem();
    if (selectedBook == null) {
      Optional<ButtonType> typeOpt = dialogUtils
          .confirm(getResource("confirm.book.download.all.title"),
              getResource("confirm.book.download.all.content"));

      if (typeOpt.isPresent() && typeOpt.get() == ButtonType.OK) {
        tableView.getItems().forEach(this::submitDownloadTask);
        infoUtils
            .showInfo(infoArea, InfoType.SUCCESS,
                new Text(getResource("success.book.start.all")));
        log.info("launch taks for all books in the table.");
      }
    } else {
      submitDownloadTask(selectedBook);
      infoUtils
          .showInfo(infoArea, InfoType.SUCCESS,
              new Text(
                  String.format(getResource("success.book.start.one"), selectedBook.getUrl())));
      log.info("launch a tak for book {}", selectedBook.getUrl());
    }
    refresh();
  }

  private void submitDownloadTask(SingleBookEntity selectedBook) {
    CustomJobParameter<SingleBookEntity> customJobParameter = new CustomJobParameter<>(
        selectedBook);

    //Launch a task to download one book
    JobParameters parameters = new JobParametersBuilder()
        .addParameter(DownloadConstants.SINGLE_BOOK_PARAM, customJobParameter).toJobParameters();
    try {
      log.info("launching a job for book: {}", selectedBook.getUrl());
      jobLauncher.run(job, parameters);
      log.info("The task is running for {}", selectedBook.getUrl());
    } catch (Exception e) {
      log.warn("failed to launch downloading task", e);
      dialogUtils.alertException(getResource("error.book.start.failed"), e);
    }
  }

  private final EhcacheUtil ehcacheUtil;

  /**
   * start tasks for downloading books marked with 'FAILED' status
   */
  public void startFailure() {
    ehcacheUtil.listKeys("books");
    tableView.getItems().stream()
        .filter(singleBookEntity -> StatusEnum.FAILED.equals(singleBookEntity.getStatus()))
        .forEach(this::submitDownloadTask);
  }

  /**
   * Bulk start for books marked with new
   */
  public void bulkStart() {
    Optional<ButtonType> buttonTypeOpt = dialogUtils
        .confirm(getResource("confirm.book.bulk.start.title"),
            getResource("confirm.book.bulk.start.content"));

    if (buttonTypeOpt.isPresent() && buttonTypeOpt.get() == ButtonType.OK) {
      List<SingleBookEntity> bookEntities = bookService
          .findByStatus(Stream.of(StatusEnum.FAILED, StatusEnum.NEW_ADDED));

      if (bookEntities.isEmpty()) {
        log.warn("No books got be downloaded and the book list size is {}", bookEntities.size());
        return;
      }

      log.info("Will download {} books.", bookEntities.size());

      //kick of tasks one by one and sleep 5 seconds for each task
      bookEntities.forEach(bookentity -> {
        submitDownloadTask(bookentity);
        infoUtils
            .showInfo(infoArea, InfoType.SUCCESS,
                new Text(
                    String.format(getResource("success.book.start.one"), bookentity.getUrl())));
        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
          log.warn("failed to sleep for 5 seconds for book {}", bookentity.getUrl(), e);
        }
      });
    }

  }

  public void showDownloadingProgress() {
    LoaderEntity loaderEntity = this.load(FxmlPath.poolInfoDialog.value());
    Dialog dialog = new Dialog();
    dialog.setTitle(getResource("dialog.book.pool.info.title"));
    dialog.setHeaderText(null);
    dialog.getDialogPane().setContent(loaderEntity.getParent());
    PoolController controller = loaderEntity.getFxmlLoader().getController();

    //auto close while click 'X'
    Window window = dialog.getDialogPane().getScene().getWindow();

    controller.setWindow(window);
    controller.setInfoArea(infoArea);
    window.setOnCloseRequest(event -> window.hide());
    dialog.showAndWait();
  }

  public void exportXml() {
    LoaderEntity loaderEntity = load(FxmlPath.exportDialog.value());
    Dialog dialog = new Dialog();
    dialog.setTitle(getResource("dialog.book.choose.dir"));
    dialog.setHeaderText(null);
    dialog.getDialogPane().setContent(loaderEntity.getParent());
    ExportController controller = loaderEntity.getFxmlLoader().getController();

    //auto close while click 'X'
    Window window = dialog.getDialogPane().getScene().getWindow();

    controller.setWindow(window);
    controller.setInfoArea(infoArea);
    window.setOnCloseRequest(event -> window.hide());
    dialog.showAndWait();
  }

  public void importXml() {
    LoaderEntity loaderEntity = load(FxmlPath.importDialog.value());

    Dialog dialog = new Dialog();
    dialog.setTitle(getResource("dialog.book.choose.file"));
    dialog.setHeaderText(null);
    dialog.getDialogPane().setContent(loaderEntity.getParent());
    ImportController controller = loaderEntity.getFxmlLoader().getController();

    //auto close while click 'X'
    Window window = dialog.getDialogPane().getScene().getWindow();

    controller.setWindow(window);
    controller.setInfoArea(infoArea);

    NoArgCallback callback = this::refresh;
    controller.setCallback(callback);

    window.setOnCloseRequest(event -> window.hide());
    dialog.showAndWait();
  }

  public void deleteAllPictures() {
    picRep.deleteAll();
    infoUtils.showInfo(infoArea, InfoType.SUCCESS,
        new Text(getResource("success.book.delete.all.pictures")));
  }

}
