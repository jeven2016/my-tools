package zjtech.piczz.downloadbook;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zjtech.modules.common.AbstractController;

@RestController
@RequestMapping("/export")
@Slf4j
public class ExportController extends AbstractController {

  @Autowired
  private BookService bookService;


  @GetMapping
  public void perform() throws IOException {

    XmlMapper xmlMapper = new XmlMapper();
    File outputFile = new File("books.xml");

    List<SingleBookEntity> bookList = bookService.findAll();
    bookList.forEach(book -> book.getPictures().clear());
    xmlMapper.writeValue(outputFile, bookList);
  }
}
