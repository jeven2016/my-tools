/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import zjtech.modules.common.*;
import zjtech.modules.common.cache.EhcacheUtil;
import zjtech.modules.utils.InfoUtils;
import zjtech.modules.utils.InfoUtils.InfoType;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;
import zjtech.piczz.downloadbook.threadpool.DownloadUtil;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static zjtech.piczz.common.DownloadConstants.SAVE_PIC;
import static zjtech.piczz.common.DownloadConstants.UPDATE_STATUS;

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

  private final Job complteBooksInfoJob;

  @FXML
  public ChoiceBox filterBox;

  @FXML
  public Text pageSizeText;

  @FXML
  public MenuItem validateDirItem;

  @FXML
  public MenuButton validateMenuBtn;

  @FXML
  private TextFlow infoArea;

  private final InfoUtils infoUtils;

  private ApplicationContext applicationContext;

  private final PicRep picRep;


  private final DownloadUtil downloadUtil;

  private static final String FILTER_ALL = "All";
  private String currentFilterType = FILTER_ALL;


  private GlobalSettingService globalSettingService;

  @Autowired
  public DownloadBookController(ApplicationContext applicationContext, DialogUtils dialogUtils,
                                BookService bookService,
                                @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
                                @Qualifier("downloadSingleBookJob") Job job,
                                InfoUtils infoUtils, PicRep picRep,
                                EhcacheUtil ehcacheUtil,
                                DownloadUtil downloadUtil,
                                GlobalSettingService globalSettingService,
                                @Qualifier("completeBookInfoJob") Job complteBooksInfoJob) {
    this.applicationContext = applicationContext;
    this.dialogUtils = dialogUtils;
    this.bookService = bookService;
    this.jobLauncher = jobLauncher;
    this.job = job;
    this.infoUtils = infoUtils;
    this.picRep = picRep;
    this.ehcacheUtil = ehcacheUtil;
    this.downloadUtil = downloadUtil;
    this.globalSettingService = globalSettingService;
    this.complteBooksInfoJob = complteBooksInfoJob;
  }


  /**
   * Add a book by entering the link
   */
  public void addBook() {
    String link = bookUrlInput.getText();
    bookUrlInput.clear();
    if (StringUtils.isEmpty(link)) {
      dialogUtils.alert(getResource("error.invalid.book.title"),
         getResource("error.invalid.book.content"));

      return;
    }
    SingleBookEntity singleBookEntity = new SingleBookEntity();
    singleBookEntity.setUrl(link.trim());
    singleBookEntity.setStatus(StatusEnum.NEW_ADDED);

    try {
      bookService.save(singleBookEntity);
    } catch (ToolException e) {
      String resource = getResource(e.getErrorCode().getCode().toString());
      infoUtils.showInfo(infoArea, InfoType.FAILURE, new Text(resource));
      log.info("Failed to insert a duplicated book for url {}", link);
      return;
    }


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

  public void deleteAllBooks() {
    Optional<ButtonType> opt = dialogUtils.confirm(getResource("confirm.book.delete.all.title"),
       getResource("confirm.book.delete.all.content"));
    if (opt.isPresent() && opt.get() == ButtonType.OK) {
      String msg = "success.book.delete.all.books";
      InfoType type = InfoType.SUCCESS;
      try {
        bookService.deleteAll();
      } catch (Exception e) {
        msg = "error.book.deletion.all.failed";
        type = InfoType.FAILURE;
      }
      infoUtils.showInfo(infoArea, type, new Text(getResource(msg)));
      refresh();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    List<String> items = Stream.of(StatusEnum.values()).map(StatusEnum::value)
       .collect(Collectors.toList());
    filterBox.getItems().addAll(FXCollections.observableArrayList(items));
    currentFilterType = "All";

    Tooltip tooltip = new Tooltip(
       "Validate From Directory: check the local directory and validate the pictures count is valid");
    Tooltip.install(validateMenuBtn, tooltip);
    refresh();
  }

  public void refresh() {
    this.refershByStatus(currentFilterType);
  }

  public void triggerAdd(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
      this.addBook();
    }
  }

  private void refershByStatus(String status) {
    List<SingleBookEntity> list;
    if (status.equals(FILTER_ALL)) {
      list = bookService.findAll();
    } else {
      StatusEnum statusEnum = StatusEnum.of(status);
      list = bookService.findByStatus(Stream.of(statusEnum));
    }
    ObservableList<SingleBookEntity> observableList = FXCollections.observableArrayList(list);
    tableView.setItems(observableList);
    pageSizeText.setText(list.size() + "");
    log.info("refresh the list of books");
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
         .findByStatus(Stream
            .of(StatusEnum.FAILED, StatusEnum.NEW_ADDED, StatusEnum.PARSING, StatusEnum.PARSED));

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
    window.setOnCloseRequest(event -> {
      window.hide();
      refresh();
    });
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

    window.setOnCloseRequest(event -> {
      window.hide();
      refresh();
    });
    dialog.showAndWait();
  }

  public void deleteAllPictures() {
    picRep.deleteAll();
    infoUtils.showInfo(infoArea, InfoType.SUCCESS,
       new Text(getResource("success.book.delete.all.pictures")));
  }

  public void filterByStatus() {
    String value = filterBox.getSelectionModel().getSelectedItem().toString();
    currentFilterType = value;
    log.info("current filter type is {}", value);
    this.refresh();
  }

  public void validateDownloadedBooks() {
    Optional<GlobalSettingEntity> optional = globalSettingService.getOne();
    if (!optional.isPresent()) {
      log.warn("No global setting exists.");
      return;
    }

    GlobalSettingEntity globalSettingEntity = optional.get();
    Path path = Paths.get(globalSettingEntity.getStorageDirectory());
    List<String> booksNotFound = new ArrayList<>();
    String bookName = "";
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
      for (Path bookPath : directoryStream) {
        File directory = bookPath.toFile();
        if (directory.isDirectory()) {
          bookName = bookPath.getFileName().toString();
          log.info("the book name is {}", bookName);
          log.info("file count is {}", directory.listFiles().length);

          SingleBookEntity bookEntity = bookService.findByName(bookName);
          if (bookEntity == null) {
            booksNotFound.add(bookName);
            log.warn("Book {} is not found in db", bookName);
            continue;
          }
          updateStatus(bookEntity, (int) (directory.listFiles().length), bookPath);
        }
      }
    } catch (Exception e) {
      log.warn("Cannot validate the books downloaded. the last book is {}", bookName, e);
    }

    try (BufferedWriter bufferedWriter = Files
       .newBufferedWriter(Paths.get("books_not_found.txt"), StandardOpenOption.APPEND,
          StandardOpenOption.CREATE);
         PrintWriter printWriter = new PrintWriter(bufferedWriter)) {
      booksNotFound.forEach(printWriter::print);
      printWriter.flush();
    } catch (IOException e) {
      log.warn("Cannot append the books into not found list.", e);
    }
    refresh();
  }


  public void validateBooks() {
    List<SingleBookEntity> bookEntityList = bookService
       .findByStatus(Stream.of(StatusEnum.PARSED, StatusEnum.PARSING, StatusEnum.COMPLETED));
    bookEntityList.forEach(book -> {
      Path bookPath = downloadUtil.getBookPath(book.getName());
      if (!bookPath.toFile().exists()) {
        return;
      }
      try {
        int fileCount = (int) Files.list(bookPath).count();
        updateStatus(book, fileCount, bookPath);
      } catch (IOException e) {
        log.warn("failed to validate books", e);
      }
    });
    refresh();
  }

  private void updateStatus(SingleBookEntity book, int fileCount, Path bookPath) {
    try {
      int picCount = book.getPicCount();
      if (picCount == 0 || picCount != fileCount) {
        //not started
        bookService.updateStatus(book.getId(), StatusEnum.INCOMPLETE);
        log.warn("{}'s count is incorrect, will fix this issue latter.", book.getName());
        return;
      }

      log.info("{}'s count is correct.", book.getName());
      if (!StatusEnum.COMPLETED.equals(book.getStatus())) {
        //correct the status
        bookService.updateStatus(book.getId(), StatusEnum.COMPLETED);
      }
    } catch (Exception e) {
      log.warn("failed to list the number of files in direct {}, exception={}",
         bookPath.toAbsolutePath(), e.getMessage());
    }
  }

  public void completeBooksInfo() {

    List<SingleBookEntity> bookEntities = bookService
       .findByStatus(Stream
          .of(StatusEnum.FAILED, StatusEnum.NEW_ADDED, StatusEnum.PARSING));

    if (bookEntities.isEmpty()) {
      log.warn("No books got be downloaded and the book list size is {}", bookEntities.size());
      return;
    }

    log.info("Will complete books info {}(count) .", bookEntities.size());
//    List<SingleBookEntity> newList = new ArrayList<>();
//    newList.add(bookEntities.get(0));

    //kick of tasks one by one and sleep 5 seconds for each task
    bookEntities.forEach(bookentity -> {
      CustomJobParameter<SingleBookEntity> customJobParameter = new CustomJobParameter<>(
         bookentity);

      CustomJobParameter<Boolean> savePicParam = new CustomJobParameter<>(false);
      CustomJobParameter updateStatusParam = new CustomJobParameter<>(false);

      //Launch a task to download one book
      JobParameters parameters = new JobParametersBuilder()
         .addParameter(DownloadConstants.SINGLE_BOOK_PARAM, customJobParameter)
         .addParameter(SAVE_PIC, savePicParam)
         .addParameter(UPDATE_STATUS, updateStatusParam)
         .toJobParameters();
      try {
        log.info("launching a complete info job for book: {}", bookentity.getUrl());
        jobLauncher.run(complteBooksInfoJob, parameters);
        log.info("The complete info task is running for {}", bookentity.getUrl());
      } catch (Exception e) {
        log.warn("failed to launch complete info job", e);
        dialogUtils.alertException(getResource("error.book.complete.failed"), e);
      }

      infoUtils
         .showInfo(infoArea, InfoType.SUCCESS,
            new Text(
               String.format(getResource("success.book.complete.books"), bookentity.getUrl())));
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (Exception e) {
        log.warn("failed to sleep for 5 seconds for book {}", bookentity.getUrl(), e);
      }
    });
  }

  public void validateUncompletedBooks() {
    List<SingleBookEntity> bookEntityList = bookService
       .findByStatus(Stream.of(StatusEnum.PARSED, StatusEnum.PARSING,
          StatusEnum.INCOMPLETE, StatusEnum.NEW_ADDED, StatusEnum.INCOMPLETE));
    bookEntityList.forEach(book -> {
      Path bookPath = downloadUtil.getBookPath(book.getName());
      if (!bookPath.toFile().exists()) {
        return;
      }
      try {
        int fileCount = (int) Files.list(bookPath).count();
        updateStatus(book, fileCount, bookPath);
      } catch (IOException e) {
        log.warn("failed to validate books", e);
      }
    });
    refresh();
  }
}
