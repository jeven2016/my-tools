package zjtech.modules.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EhcacheUtil {

  private final CacheManager cacheManager;

  @Autowired
  public EhcacheUtil(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  public void listKeys(String cacheName) {
    Cache cache = this.cacheManager.getCache(cacheName);
    if (cache == null) {
      log.warn("the cache '{}' not found.", cacheName);
      return;
    }

    boolean isInstance = cacheManager instanceof EhCacheCacheManager;
    log.info("isInstance={}", isInstance);
/*
    net.sf.ehcache.EhCache  ehCache = ( net.sf.ehcache.EhCache ) cache;
    ehCache.getConfig().get
*/

  }

}
