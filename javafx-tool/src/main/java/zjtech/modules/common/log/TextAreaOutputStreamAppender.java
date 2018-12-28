package zjtech.modules.common.log;

import ch.qos.logback.core.OutputStreamAppender;
import java.io.OutputStream;

/**
 * The appender used to output log to a textarea in GUI
 */
public class TextAreaOutputStreamAppender<E> extends OutputStreamAppender<E> {

  private static DelegatingOutputStream DELEGATING_OUTPUT_STREAM = new DelegatingOutputStream();

  @Override
  public void start() {
    setOutputStream(DELEGATING_OUTPUT_STREAM);

    super.start();
  }

  public static void setStaticOutputStream(OutputStream outputStream) {
    DELEGATING_OUTPUT_STREAM.setOutputStream(outputStream);
  }
}
