package zjtech.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import zjtech.piczz.downloadbook.DownloadBookController;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DownloadBookControllerTest {
  @Autowired
  private DownloadBookController controller;

  @Test
  public void testComplete() {
    controller.completeBooksInfo();
  }
}
