package zjtech.piczz.downloadbook.jobs;

import java.io.IOException;
import java.util.Optional;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

  @Value("${page.subpage.count}")
  String pagesCountReg;

  @Autowired
  public BookPageParserTask(GlobalSettingService globalSettingService, BookService bookService) {
    this.globalSettingService = globalSettingService;
    this.bookService = bookService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    //get the book entity form job parameters
    SingleBookEntity singleBookEntity = (SingleBookEntity) chunkContext.getStepContext()
        .getJobParameters().get(DownloadConstants.SINGLE_BOOK_PARAM);

    try {
      singleBookEntity = handle(singleBookEntity);

      //set the single book entity
      ExecutionContext context = chunkContext.getStepContext().getStepExecution().getJobExecution()
          .getExecutionContext();
      context.put(DownloadConstants.SINGLE_BOOK_PARAM, singleBookEntity);
    } catch (Exception e) {
      bookService.updateStatus(singleBookEntity.getId(), StatusEnum.FAILED);
    }
    return RepeatStatus.FINISHED;
  }

  private SingleBookEntity handle(SingleBookEntity singleBookEntity) throws IOException {
    Optional<GlobalSettingEntity> globalSettingEntityOptional = globalSettingService.getOne();
    int timeout = 10000;
    if (globalSettingEntityOptional.isPresent()) {
      timeout = globalSettingEntityOptional.get().getDownloadTimeout();
    }

    //update status
    singleBookEntity.getPictures().clear();//clear old pictures

    bookService.updateStatus(singleBookEntity.getId(), StatusEnum.PARSING);

    // find the sub page count
    Document document = Jsoup.connect(singleBookEntity.getUrl()).timeout(timeout).get();
    Elements elements = document.select(pagesCountReg);
    String href = elements.first().attr("href");

    if (StringUtils.isEmpty(href)) {
      log.error("Cannot get the link of book: " + singleBookEntity.getName());
      return singleBookEntity;
    }

    if (href.endsWith("/")) {
//      href = href.substring(0, href.lastIndexOf("/"));
    }

//    int splitIndex = href.lastIndexOf("/");
//    String prefix = href.substring(0, splitIndex);
//    int count = Integer.parseInt(href.substring(splitIndex + 1));//count of sub pages
    int count = elements.size();
    singleBookEntity.setPicPageCount(count);

    //TODO: parese the name this book from html page
    Element nameElem = document.selectFirst(".title-warper .title");
    singleBookEntity.setName(nameElem.text());

    int picCount = 0;

    // navigate to each sub page
    int i = 1;
    for (Element elem : elements) {
      document = Jsoup.connect(elem.attr("href")).timeout(timeout).get();

      Elements imgs = document.select("img[class^='comicimg']");

      int imgIndex = 1;
      for (Element img : imgs) {
        SinglePictureEntity pictureEntity = new SinglePictureEntity();
        pictureEntity.setUrl(img.attr("src"));
        pictureEntity.setBooKName(singleBookEntity.getName());
        pictureEntity.setPicIndex(imgIndex++);
        pictureEntity.setSubPageNo(i);
        singleBookEntity.addPicture(pictureEntity);

        picCount++;
      }

      i++;
    }

    singleBookEntity.setPicCount(picCount);

    log.info("{} pictures of book {} will be downloaded....", picCount, singleBookEntity.getName());

    //update the book entity in db
    singleBookEntity.setStatus(StatusEnum.PARSED);
    return bookService.save(singleBookEntity);
  }
}
