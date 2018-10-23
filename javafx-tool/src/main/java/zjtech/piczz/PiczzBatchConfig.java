package zjtech.piczz;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ImportResource(value = {
    "classpath:piczz_jobs/download-singlebook.xml"})
public class PiczzBatchConfig {

  @Value("${task.thread-pool}")
  private int threadPoolCount;


  @Bean
  public TaskExecutor customTaskExecutor() {
    ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
    threadPool.setCorePoolSize(threadPoolCount);
    return threadPool;
  }

  @Bean
  public SimpleAsyncTaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

}