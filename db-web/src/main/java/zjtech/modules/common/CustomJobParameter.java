package zjtech.modules.common;

import org.springframework.batch.core.JobParameter;

import java.util.UUID;

/**
 * Cannot send object as JobParameter in spring batch need custom implementation
 * https://stackoverflow.com/questions/33761730/how-to-send-a-custom-object-as-job-parameter-in-spring-batch
 */
public class CustomJobParameter<T> extends JobParameter {

  private T customParam;

  public CustomJobParameter(T customParam) {
    super(UUID.randomUUID().toString());//This is to avoid duplicate JobInstance error
    this.customParam = customParam;
  }

  public T getValue() {
    return customParam;
  }
}
