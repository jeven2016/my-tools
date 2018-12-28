package zjtech.common.jpa;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import zjtech.DpApplication;
import zjtech.piczz.downloadbook.BookService;
import zjtech.piczz.downloadbook.SingleBookEntity;

import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DpApplication.class)
public class LazyTest {
  @Autowired
  private BookService bookService;

  @Test
  public void test_only_query_books() {

    List<SingleBookEntity> list = bookService
        .findByStatus(Stream.of(SingleBookEntity.StatusEnum.COMPLETED));

    list = bookService.findAll();

    System.out.println(list.get(0).getPictures().size());
    System.out.println(list.get(1));
  }

}
