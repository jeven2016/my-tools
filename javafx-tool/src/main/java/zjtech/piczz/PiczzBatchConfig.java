package zjtech.piczz;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


  /**
   * Init thread pool for asynchronous task
   *
   * @return TaskExecutor
   */
  @Bean
  public TaskExecutor customTaskExecutor() {
    ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
    threadPool.setCorePoolSize(threadPoolCount);
    threadPool.setMaxPoolSize(threadPoolCount);
    threadPool.afterPropertiesSet();
    return threadPool;
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  /**
   * Asynchronous job launcher
   *
   * @return SimpleAsyncTaskExecutor
   */
  @Bean
  public SimpleAsyncTaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean("asyncJobLauncher")
  public JobLauncher jobLauncher(@Autowired JobRepository jobRepository) throws Exception {
    SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(taskExecutor());
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
}