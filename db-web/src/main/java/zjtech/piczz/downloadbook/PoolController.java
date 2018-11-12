package zjtech.piczz.downloadbook;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.event.ActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zjtech.modules.common.AbstractController;
import zjtech.piczz.downloadbook.threadpool.DownloadingThreadPool;

@RestController
@RequestMapping("/pool")
@Lazy
@Slf4j
public class PoolController extends AbstractController {


  private final DownloadingThreadPool pool;

  @Autowired
  public PoolController(DownloadingThreadPool pool) {
    this.pool = pool;
  }


  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Mono<Map<String, String>> refresh(ActionEvent actionEvent) {
    int currentSize = pool.getPoolSize();
    int threadSize = pool.getThreadCount();

    Map<String, String> map = new LinkedHashMap<>();
    map.put("currentPoolSize", currentSize + "");
    map.put("threadSize", threadSize + "");
    return Mono.just(map);
  }

}
