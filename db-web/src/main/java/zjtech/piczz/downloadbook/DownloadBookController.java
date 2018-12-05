/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.CustomJobParameter;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;
import zjtech.piczz.downloadbook.threadpool.DownloadUtil;
import zjtech.piczz.downloadbook.threadpool.DownloadingThreadPool;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

@RestController
@RequestMapping("/book")
@Slf4j
public class DownloadBookController extends AbstractController {

  @Autowired
  private BookService bookService;

  @Autowired
  private JobLauncher jobLauncher;

  @Autowired
  private Job job;

  @Autowired
  private PicRep picRep;

  @Autowired
  private DownloadingThreadPool downloadingThreadPool;


  private static final String FILTER_ALL = "all";
  private String currentFilterType = FILTER_ALL;


  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private DownloadUtil downloadUtil;


  /**
   * Add a book by entering the link
   */
  @GetMapping("/add")
  @ResponseStatus(HttpStatus.CREATED)
  public void addBook(@RequestParam("link") String link) {
    if (StringUtils.isEmpty(link)) {
      throw new IllegalArgumentException("invalid url you input");
    }
    SingleBookEntity singleBookEntity = new SingleBookEntity();
    singleBookEntity.setUrl(link);
    singleBookEntity.setStatus(StatusEnum.NEW_ADDED);
    try {
      bookService.save(singleBookEntity);
    } catch (Exception e) {
      throw new RuntimeException("Failed to insert a duplicated book for url {}" + link, e);
    }
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(long id) {
    bookService.delete(id);
  }

  @GetMapping("/delete/all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAllBooks() {
    bookService.deleteAll();
  }

  @GetMapping("list/{status}")
  public Flux<List<SingleBookEntity>> refershByStatus(@PathVariable String status) {
    List<SingleBookEntity> list;
    if (status.equals(FILTER_ALL)) {
      list = bookService.findAll();
    } else {
      StatusEnum statusEnum = StatusEnum.ofLabel(status);
      list = bookService.findByStatus(Stream.of(statusEnum));
    }
    log.info("refresh the list of books");
    return Flux.just(list);
  }


  /**
   * Start task(s)
   */
  @GetMapping("start/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void startOne(@PathVariable("id") long id) {
    SingleBookEntity selectedBook = bookService.findById(id);
    submitDownloadTask(selectedBook);
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
    }
  }


  /**
   * start tasks for downloading books marked with 'FAILED' status
   */
  @GetMapping("/failed")
  @ResponseStatus(HttpStatus.OK)
  public void startFailure() {
    bookService.findByStatus(Stream.of(StatusEnum.FAILED)).stream()
        .filter(singleBookEntity -> StatusEnum.FAILED.equals(singleBookEntity.getStatus()))
        .forEach(this::submitDownloadTask);
  }

  @GetMapping("smart")
  public void smartStart() {
    List<SingleBookEntity> bookEntities = getSingleBookEntities();

    if (bookEntities.size() <= 3) {
      bulkStart();
      return;
    }

    //first start 3 jobs
    for (int i = 0; i < bookEntities.size(); i++) {
      if (i < 3) {
        log.info("start a job{} for {}", i, bookEntities.get(i).getUrl());
        submitDownloadTask(bookEntities.get(i));
      }
    }

    //start other jobs periodically
    bookEntities = bookEntities.subList(3, bookEntities.size());

    Iterator<SingleBookEntity> itr = bookEntities.iterator();
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        int poolsize = downloadingThreadPool.getPoolSize();
        int capacity = downloadingThreadPool.getCapacity();
        if (poolsize >= capacity - 100) {
          return;
        }
        if (!itr.hasNext()) {
          log.warn("[[[[[Timer]]]]]]the timer will stop latter");
          timer.cancel();
          return;
        }
        log.info("[[[[[Timer-submit-task]]]]]]the timer will submit a task latter");
        submitDownloadTask(itr.next());
        log.info("[[[[[Timer-submit-task]]]]]] a task");
      }
    }, 2000, 5000);
  }

  /**
   * Bulk start for books marked with new
   */
//  @GetMapping("/bulk")
//  @ResponseStatus(HttpStatus.OK)
  public void bulkStart() {
    List<SingleBookEntity> bookEntities = getSingleBookEntities();

    if (bookEntities.isEmpty()) {
      log.warn("No books got be downloaded and the book list size is {}", bookEntities.size());
      return;
    }

    log.info("Will download {} books.", bookEntities.size());
    launchTasks(bookEntities);

  }

  private void launchTasks(List<SingleBookEntity> bookEntities) {
    //kick of tasks one by one and sleep 5 seconds for each task
    bookEntities.forEach(bookentity -> {
      submitDownloadTask(bookentity);
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (Exception e) {
        log.warn("failed to sleep for 5 seconds for book {}", bookentity.getUrl(), e);
      }
    });
  }

  private List<SingleBookEntity> getSingleBookEntities() {
    return bookService
        .findByStatus(Stream
            .of(StatusEnum.FAILED, StatusEnum.NEW_ADDED, StatusEnum.PARSING, StatusEnum.PARSED,
                StatusEnum.INCOMPLETE)
        );
  }


  @GetMapping("delete/pic")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAllPictures() {
    picRep.deleteAll();

  }


  @GetMapping("/validate/local")
  @ResponseStatus(HttpStatus.OK)
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
  }


  @GetMapping("/validate/remote")
  @ResponseStatus(HttpStatus.OK)
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

  public void addPage(ActionEvent actionEvent) {


  }
}
