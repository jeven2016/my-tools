package zjtech.piczz.downloadbook.jobs;

import java.util.Set;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import zjtech.piczz.common.DownloadConstants;
import zjtech.piczz.downloadbook.SingleBookEntity;
import zjtech.piczz.downloadbook.SinglePictureEntity;
import zjtech.piczz.downloadbook.threadpool.DownloadingThreadPool;

@Component
public class DownloadSingleBookTask implements Tasklet {

  @Autowired
  DownloadingThreadPool downloadingThreadPool;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
      throws Exception {
    SingleBookEntity singleBookEntity = (SingleBookEntity) chunkContext.getStepContext()
        .getStepExecution().getJobExecution()
        .getExecutionContext().get(DownloadConstants.SINGLE_BOOK_PARAM);

    Set<SinglePictureEntity> set = singleBookEntity.getPictures();
    downloadingThreadPool.run();
    downloadingThreadPool.addPictures(set);
    return RepeatStatus.FINISHED;
  }
}
