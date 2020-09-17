package zjtech.piczz.downloadbook.jobs;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.BookService;
import zjtech.piczz.downloadbook.SingleBookEntity;
import zjtech.piczz.downloadbook.SingleBookEntity.StatusEnum;
import zjtech.piczz.downloadbook.SinglePictureEntity;
import zjtech.piczz.downloadbook.threadpool.DownloadingThreadPool;

@Component
@Slf4j
public class DownloadSingleBookTask implements Tasklet {

  private final DownloadingThreadPool downloadingThreadPool;

  private final BookService bookService;

  @Autowired
  public DownloadSingleBookTask(DownloadingThreadPool downloadingThreadPool,
      BookService bookService) {
    this.downloadingThreadPool = downloadingThreadPool;
    this.bookService = bookService;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    SingleBookEntity singleBookEntity = (SingleBookEntity) chunkContext.getStepContext()
        .getStepExecution().getJobExecution()
        .getExecutionContext().get(DownloadConstants.SINGLE_BOOK_PARAM);
    if(singleBookEntity == null){
      log.error("No book entity passed to download task");
      return RepeatStatus.FINISHED;
    }
    bookService.updateStatus(singleBookEntity.getId(), StatusEnum.DWONLOADING);
    if (singleBookEntity == null) {
      log.warn("book entity is null and the task have to finish.");
      bookService.updateStatus(singleBookEntity.getId(), StatusEnum.IGNORED);
      return RepeatStatus.FINISHED;
    }

    Set<SinglePictureEntity> set = singleBookEntity.getPictures();
    downloadingThreadPool.run();
    downloadingThreadPool.addPictures(set);
    bookService.updateStatus(singleBookEntity.getId(), StatusEnum.COMPLETED);
    return RepeatStatus.FINISHED;
  }
}
