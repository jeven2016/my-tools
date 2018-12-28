package zjtech.common;


import java.util.ArrayList;
import java.util.Objects;
import javax.cache.Cache;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;
import zjtech.DpApplication;
import zjtech.piczz.gs.GlobalSettingService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DpApplication.class)
public class CacheUtilTest {

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private DataSource dataSource;


  @Test
  public void validateCache() {
    new ArrayList<>(this.cacheManager.getCacheNames())
        .forEach(name -> System.out
            .println("The cache found: " + name));

    //get jsr Cache instead of spring Cache
    Cache gsCache = getCache("globalSetting");
    Objects.requireNonNull(gsCache).forEach(System.out::println);
  }

  @Test
  public void testGsCache() {
    globalSettingService.getOne();
    globalSettingService.getOne();
    globalSettingService.getOne();

    Cache gsCache = getCache("globalSetting");
    gsCache.forEach(key -> {
      System.out.println("The key exists: " + key);
    });

  }

  @Test
  public void dataSourceTest() {
    System.out.println(dataSource.getClass());
  }


  private Cache getCache(String cacheName) {
    return (Cache) this.cacheManager.getCache(cacheName).getNativeCache();
  }
}
