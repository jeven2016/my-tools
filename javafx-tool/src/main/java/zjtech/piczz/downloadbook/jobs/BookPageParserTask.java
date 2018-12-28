package zjtech.piczz.downloadbook.jobs;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.BookService;
import zjtech.piczz.downloadbook.SingleBookEntity;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;
import zjtech.piczz.downloadbook.SinglePictureEntity;
import zjtech.piczz.gs.GlobalSettingEntity;
import zjtech.piczz.gs.GlobalSettingService;

@Component
@Slf4j
public class BookPageParserTask implements Tasklet {

  private final GlobalSettingService globalSettingService;

  private final BookService bookService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public BookPageParserTask(GlobalSettingService globalSettingService, BookService bookService, ObjectMapper objectMapper) {
    this.globalSettingService = globalSettingService;
    this.bookService = bookService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
     throws Exception {
    //get the book entity form job parameters
    SingleBookEntity singleBookEntity = (SingleBookEntity) chunkContext.getStepContext()
       .getJobParameters().get(DownloadConstants.SINGLE_BOOK_PARAM);

    //save pic as well
    Boolean savePic = (Boolean) chunkContext.getStepContext()
       .getJobParameters().get(DownloadConstants.SAVE_PIC);
    savePic = savePic == null ? true : false;

    //should update this book status
    Boolean updateStatus = (Boolean) chunkContext.getStepContext()
       .getJobParameters().get(DownloadConstants.UPDATE_STATUS);
    updateStatus = updateStatus == null ? true : false;

    try {
      singleBookEntity = handle(singleBookEntity, savePic, updateStatus);

      //set the single book entity
      ExecutionContext context = chunkContext.getStepContext().getStepExecution().getJobExecution()
         .getExecutionContext();
      context.put(DownloadConstants.SINGLE_BOOK_PARAM, singleBookEntity);
    } catch (Exception e) {
      if (updateStatus) {
        bookService.updateStatus(singleBookEntity.getId(), StatusEnum.FAILED);
      }
    }
    return RepeatStatus.FINISHED;
  }

  private SingleBookEntity handle(SingleBookEntity singleBookEntity, Boolean savePic, boolean updateStatus) throws IOException {
    Optional<GlobalSettingEntity> globalSettingEntityOptional = globalSettingService.getOne();
    int timeout = 10000;
    if (globalSettingEntityOptional.isPresent()) {
      timeout = globalSettingEntityOptional.get().getDownloadTimeout();
    }

    //update status
    singleBookEntity.getPictures().clear();//clear old pictures

    if (updateStatus) {
      bookService.updateStatus(singleBookEntity.getId(), StatusEnum.PARSING);
    }

    // find the sub page count
    Document document = Jsoup.connect(singleBookEntity.getUrl()).timeout(timeout).get();
    Elements elements = document.select(".wp-pagenavi a:nth-last-child(2)");
    String href = elements.first().attr("href");

    int splitIndex = href.lastIndexOf("/");
    String prefix = href.substring(0, splitIndex);
    int count = Integer.parseInt(href.substring(splitIndex + 1));//count of sub pages
    singleBookEntity.setPicPageCount(count);

    //TODO: parese the name this book from html page
    Element nameElem = document.selectFirst("h1[class='entry-title']");
    singleBookEntity.setName(nameElem.text());

    int picCount = 0;
    // navigate to each sub page
    for (int i = 1; i <= count; i++) {
      document = Jsoup.connect(prefix + "/" + i).timeout(timeout).get();
      Elements imgs = document.select("img[class^='alignnone size-']");

      int imgIndex = 1;
      for (Element img : imgs) {
        if (savePic) {
          SinglePictureEntity pictureEntity = new SinglePictureEntity();
          pictureEntity.setUrl(img.attr("src"));
          pictureEntity.setBooKName(singleBookEntity.getName());
          pictureEntity.setPicIndex(imgIndex++);
          pictureEntity.setSubPageNo(i);
          singleBookEntity.addPicture(pictureEntity);
        }
        picCount++;
      }
    }

    singleBookEntity.setPicCount(picCount);

    log.info("{} pictures of book {} will be downloaded....", picCount, singleBookEntity.getName());

    if (updateStatus) {
      //update the book entity in db
      singleBookEntity.setStatus(StatusEnum.PARSED);
    }

    SingleBookEntity newBookEntity = bookService.save(singleBookEntity);

    if (newBookEntity == null) {
      log.error("No book will be passed to download task, the origin book is \n {}",
         objectMapper.writeValueAsString(singleBookEntity));
    }
    return newBookEntity;
  }
}
