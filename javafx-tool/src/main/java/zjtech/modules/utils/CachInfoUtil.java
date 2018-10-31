package zjtech.modules.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CachInfoUtil {

  @Autowired
  private CacheManager cacheManager;


  public void printCacheInfo() {
    Cache cache = cacheManager.getCache("gs");
  }

}
