package zjtech.piczz.downloadbook;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import zjtech.modules.common.AbstractController;

@RestController
@RequestMapping("/import")
@Slf4j
public class ImportController extends AbstractController {

  @Autowired
  private BookService bookService;


  @GetMapping
  @ResponseStatus(value = HttpStatus.OK)
  public void perform() throws IOException {
    String path="books.xml";
    boolean isFile = Paths.get(path).toFile().isFile();
    if (!isFile) {
      throw new IllegalArgumentException(getResource("dialog.book.choose.dir.content"));
    }
    XmlMapper xmlMapper = new XmlMapper();
    File outputFile = new File(path);

    Set<SingleBookEntity> list = xmlMapper
        .readValue(outputFile, new TypeReference<Set<SingleBookEntity>>() {
        });
    bookService.saveList(list);
  }
}
