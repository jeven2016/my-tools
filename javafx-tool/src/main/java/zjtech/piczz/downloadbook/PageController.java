package zjtech.piczz.downloadbook;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import zjtech.modules.common.AbstractController;
import zjtech.modules.common.DialogUtils;
import zjtech.modules.common.ToolException;
import zjtech.modules.utils.InfoUtils;

import javax.tools.Tool;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class PageController extends AbstractController {
  @FXML
  public TextField pageUrlInput;

  @FXML
  public Button scanPageBtn;

  @FXML
  public TableView tableView;

  @FXML
  public Button addBooksBtn;

  @FXML
  public TextField excludedBookField;

  @FXML
  public Text bookSize;

  @FXML
  private TextFlow infoArea;

  @Autowired
  private DialogUtils dialogUtils;

  @Autowired
  private BookService bookService;

  @Autowired
  private InfoUtils infoUtils;

  private List<SingleBookEntity> list = new ArrayList<>();

  public void triggerScan() throws IOException {
    String link = pageUrlInput.getText();
    pageUrlInput.clear();
    if (StringUtils.isEmpty(link)) {
      dialogUtils.alert(getResource("error.page.invalid.title"),
          getResource("error.page.invalid.url"));

      return;
    }

    list.clear();
    String[] excludes = getExcludedNames();

    // navigate to the book
    Document doc = Jsoup.connect(link).timeout(30000).get();

    // find the books
    Elements books = doc.getElementsByClass("grid-title");

    books.forEach(book -> {
      //get title link
      Elements elements = book.select("a[rel=bookmark]");
      if (elements.size() > 1) {
        throw new IllegalStateException("invlid bookmark found");
      }

      Element element = elements.first();
      String name = element.text();
      String url = element.attr("href");


      if (excludes.length == 0) {
        addIntoList(name, url);
      } else {
        boolean isExcludedBook = false;
        for (String exName : excludes) {
          if (name.contains(exName.trim()) || url.contains(exName.trim())) {
            isExcludedBook = true;
          }
        }

        if (!isExcludedBook) {
          addIntoList(name, url);
        }
      }

    });

    bookSize.setText(list.size() + "");
    ObservableList<SingleBookEntity> observableList = FXCollections.observableArrayList(list);
    tableView.setItems(observableList);
    String msg = String.format(getResource("success.page.scan"));
    infoUtils.showInfo(infoArea, InfoUtils.InfoType.SUCCESS, new Text(msg));
  }

  private void addIntoList(String name, String url) {
    SingleBookEntity singleBookEntity = new SingleBookEntity();
    singleBookEntity.setName(name);
    singleBookEntity.setUrl(url);
    singleBookEntity.setStatus(SingleBookEntity.StatusEnum.NEW_ADDED);
    list.add(singleBookEntity);
  }

  public void addBooks() {
    bookService.fixNullStatus();

    Iterator<SingleBookEntity> iterator = list.listIterator();
    while (iterator.hasNext()) {
      SingleBookEntity book = iterator.next();
      if (bookService.findByUrl(book.getUrl().trim()) != null) {
        log.warn("the url is duplicated. url=" + book.getUrl());
        continue;
      }
      try {
        bookService.save(book);
        iterator.remove();
        log.info("book[name={}, url={}] is inserted.", book.getName(), book.getUrl());
      } catch (ToolException e) {
        log.warn("book[name={}, url={}] is failed to insert, exception=duplicated",
            book.getName(), book.getUrl());
      } catch (Exception e) {
        log.warn("book[name={}, url={}] is failed to insert, exception={}",
            book.getName(), book.getUrl(), e.getMessage());
      }
    }
    bookSize.setText(list.size() + "");
    ObservableList<SingleBookEntity> observableList = FXCollections.observableArrayList(list);
    tableView.setItems(observableList);
  }

  public void enterScan(KeyEvent keyEvent) throws IOException {
    if (keyEvent.getCode().equals(KeyCode.ENTER)) {
      this.triggerScan();
    }
  }

  private String[] getExcludedNames() {
    String link = excludedBookField.getText();
    if (StringUtils.isEmpty(link)) {
      return new String[0];
    }
    String[] excludeNames = {link};
    if (link.contains(",")) {
      excludeNames = link.split(",");
    }
    return excludeNames;
  }

  public void addExcludedBook(KeyEvent keyEvent) {
    if (!keyEvent.getCode().equals(KeyCode.ENTER)) {
      return;
    }
    String link = excludedBookField.getText();
    Iterator<SingleBookEntity> iterator = list.iterator();
    boolean excluded = false;
    String name = null, url = null;
    while (iterator.hasNext()) {
      SingleBookEntity bookEntity = (SingleBookEntity) iterator.next();
      name = bookEntity.getName();
      url = bookEntity.getUrl();

      for (String exName : getExcludedNames()) {
        if (name.contains(exName.trim()) || url.contains(exName.trim())) {
          iterator.remove();
          excluded = true;
          break;
        }
      }
    }

    if (excluded) {
      String msg = String.format("Exclude on book,name=%s, url=%s", name, url);
      infoUtils.showInfo(infoArea, InfoUtils.InfoType.SUCCESS, new Text(msg));
    }
    ObservableList<SingleBookEntity> observableList = FXCollections.observableArrayList(list);
    tableView.setItems(observableList);

    bookSize.setText(list.size() + "");
  }
}
